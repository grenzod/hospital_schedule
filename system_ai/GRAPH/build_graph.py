from pathlib import Path
import sys
from typing import Dict, List, Optional, Union
from typing_extensions import TypedDict, Literal
from langgraph.graph import StateGraph, START, END
import dotenv
from langchain.chat_models import init_chat_model
from pydantic import BaseModel, Field
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.output_parsers import StrOutputParser
PROJECT_ROOT = Path(__file__).parent.parent
sys.path.append(str(PROJECT_ROOT))
from RAG_SQL.main_rag_sql import get_review_response
from RAG_PDF.main_rag_file import generate_answer, get_response

dotenv.load_dotenv()

llm = init_chat_model("llama3-70b-8192", model_provider="groq")
router_llm = init_chat_model("llama3-8b-8192", model_provider="groq")

# Define the routing schema
class RouterOutput(BaseModel):
    step: Literal["sql_rag", "pdf_rag", "general"] = Field(
        description="The next step in the routing process"
    )
    confidence: float = Field(
        description="Confidence score between 0.0 and 1.0 for the chosen route",
        ge=0.0,
        le=1.0
    )
    reasoning: str = Field(
        description="Detailed reasoning for why this route was chosen"
    )
router = router_llm.with_structured_output(RouterOutput)

class State(TypedDict):
    input: str
    decision: Dict[str, Union[str, float, str]]
    output: StrOutputParser
    context: Optional[str]
    conversation_history: List[Dict[str, str]]
    source: Optional[str]
    fallback_attempts: int

def sql_rag_node(state: State) -> Dict:
    try:
        result = get_review_response(state["input"])
        if not result or len(result.strip()) < 10:
            return {
                "output": "I couldn't find relevant database information. Would you like me to search our medical documents instead?",
                "source": "sql_rag",
                "fallback_attempts": state.get("fallback_attempts", 0) + 1
            }
        return {
            "output": result,
            "source": "sql_rag",
            "conversation_history": state.get("conversation_history", []) + [
                {"role": "user", "content": state["input"]},
                {"role": "assistant", "content": result, "source": "sql_rag"}
            ]
        }
    except Exception as e:
        return {
            "output": f"I encountered an issue while searching the database. Let me try a different approach.",
            "fallback_attempts": state.get("fallback_attempts", 0) + 1
        }
    
def pdf_rag_node(state: State) -> Dict:
    try:
        result = get_response(state["input"])
        if not result or len(result.strip()) < 10:
            return {
                "output": "I couldn't find relevant information in our medical documents. Let me provide a general response.",
                "source": "pdf_rag",
                "fallback_attempts": state.get("fallback_attempts", 0) + 1
            }
        return {
            "output": result,
            "source": "pdf_rag",
            "conversation_history": state.get("conversation_history", []) + [
                {"role": "user", "content": state["input"]},
                {"role": "assistant", "content": result, "source": "pdf_rag"}
            ]
        }
    except Exception as e:
        return {
            "output": f"I encountered an issue while searching our medical documents. Let me try a different approach.",
            "fallback_attempts": state.get("fallback_attempts", 0) + 1
        }

def general_qa_node(state: State) -> Dict:
    messages = [HumanMessage(content=state["input"])]
    
    # Add conversation history context if available
    if state.get("conversation_history"):
        context = "Previous conversation:\n"
        for entry in state["conversation_history"][-3:]:  # Last 3 exchanges
            context += f"{entry['role']}: {entry['content']}\n"
        messages.insert(0, SystemMessage(content=f"Consider this conversation history for context: {context}"))
    
    result = llm.invoke(messages)
    return {
        "output": result.content,
        "source": "general",
        "conversation_history": state.get("conversation_history", []) + [
            {"role": "user", "content": state["input"]},
            {"role": "assistant", "content": result.content, "source": "general"}
        ]
    }

def router_node(state: State) -> Dict:
    # Initialize conversation history if not present
    if "conversation_history" not in state:
        state["conversation_history"] = []
    
    # Initialize fallback attempts if not present
    if "fallback_attempts" not in state:
        state["fallback_attempts"] = 0
    
    # Construct prompt with conversation history
    conversation_context = ""
    if state["conversation_history"]:
        conversation_context = "\nRecent conversation history:\n"
        for entry in state["conversation_history"][-3:]:
            conversation_context += f"{entry['role'].capitalize()}: {entry['content']}\n"
    
    decision = router.invoke(
        [
            SystemMessage(
                content=f"""You are a specialized medical query router. Your task is to analyze the input query and determine the MOST APPROPRIATE processing step. Choose one of:

                - 'sql_rag': For queries about:
                  * Database information, structured data questions
                  * Doctor reviews, ratings, or performance metrics
                  * Patient feedback or experiences with specific doctors
                  * Any queries requiring retrieval from structured databases
                  * Examples: "What's Dr. Smith's rating?", "Show patient feedback for Dr. Johnson"

                - 'pdf_rag': For queries about:
                  * Disease symptoms, treatments, or medical conditions
                  * Healthcare procedures or medical advice
                  * Medical guidelines or protocols
                  * Any queries requiring retrieval from medical documents
                  * Examples: "What are the symptoms of dengue?", "How is malaria treated?"

                - 'general': For queries that are:
                  * Non-medical, casual, or general conversation
                  * Not requiring specific medical data
                  * Follow-up questions about previous responses
                  * Examples: "Thank you", "Can you explain that more simply?"

                Focus on the core content of the query and ignore greetings or polite phrases. 
                Select the most appropriate step based on the query's intent and content.
                Provide a confidence score between 0 and 1.
                {conversation_context}
                """
            ),
            HumanMessage(content=state["input"]),
        ]
    )
    
    return {"decision": {
        "step": decision.step,
        "confidence": decision.confidence,
        "reasoning": decision.reasoning
    }}

def fallback_router(state: State) -> str:
    """Determine if we need to fallback to another approach."""
    if state.get("fallback_attempts", 0) >= 2:
        return "Node_3_general"  # After 2 failed attempts, go to general QA
    
    # Check if we've already tried the current source
    if state.get("source") == "sql_rag":
        return "Node_2_pdf_rag"
    elif state.get("source") == "pdf_rag":
        return "Node_3_general"
    
    # Default routing
    decision = state["decision"]["step"]
    confidence = state["decision"]["confidence"]
    
    # Low confidence routing
    if confidence < 0.7:
        if decision == "sql_rag":
            if "database" in state["input"].lower() or "doctor" in state["input"].lower():
                return "Node_1_sql_rag"
            else:
                return "Node_2_pdf_rag"
        elif decision == "pdf_rag":
            if "disease" in state["input"].lower() or "symptom" in state["input"].lower():
                return "Node_2_pdf_rag"
            else:
                return "Node_3_general"
    
    # High confidence routing
    if decision == "sql_rag":
        return "Node_1_sql_rag"
    elif decision == "pdf_rag":
        return "Node_2_pdf_rag"
    else:
        return "Node_3_general"

router_builder = StateGraph(State)

router_builder.add_node("Router", router_node)
router_builder.add_node("Node_1_sql_rag", sql_rag_node)
router_builder.add_node("Node_2_pdf_rag", pdf_rag_node)
router_builder.add_node("Node_3_general", general_qa_node)

router_builder.add_edge(START, "Router")
router_builder.add_conditional_edges(
    "Router",
    fallback_router,
    {
        "Node_1_sql_rag": "Node_1_sql_rag",
        "Node_2_pdf_rag": "Node_2_pdf_rag",
        "Node_3_general": "Node_3_general",
    },
)

router_builder.add_conditional_edges(
    "Node_1_sql_rag",
    lambda state: "Router" if state.get("fallback_attempts", 0) > 0 else END,
    {
        "Router": "Router",
        END: END
    }
)
router_builder.add_conditional_edges(
    "Node_2_pdf_rag",
    lambda state: "Router" if state.get("fallback_attempts", 0) > 0 else END,
    {
        "Router": "Router",
        END: END
    }
)
router_builder.add_edge("Node_3_general", END)
router_workflow = router_builder.compile()

# Example usage
# state = router_workflow.invoke({"input": "What do people say about Dr.Burh Burh?"})
# print(f"Decision: {state['decision']['step']} (Confidence: {state['decision']['confidence']})")
# print(f"Reasoning: {state['decision']['reasoning']}")
# print(f"Output: {state['output']}")
