import os
import sys
from pathlib import Path
os.environ["TOKENIZERS_PARALLELISM"] = "false"
from multiprocessing import freeze_support
PROJECT_ROOT = Path(__file__).parent
DATA_DIR = PROJECT_ROOT / "data_source" / "generative_ai"
sys.path.append(str(PROJECT_ROOT))
from src.base.llm_model import get_groq_llm
from src.rag.main import build_rag_chain, InputQA, OutputQA

def initialize_chain():
    llm = get_groq_llm(temperature=0.9)
    chain = build_rag_chain(llm, data_dir=str(DATA_DIR), data_type="pdf")
    return chain

def generate_answer(question: InputQA, chain) -> OutputQA:
    answer = chain.invoke(question.question)
    return {"answer": answer}

def get_response(question: str) -> str: 
    try:
        genai_chain = initialize_chain()
        return generate_answer(InputQA(question=question), genai_chain)
    except Exception as e:
        print(f"Error generating answer: {str(e)}")
        return {"answer": "Sorry, I encountered an error while generating the answer."}

# if __name__ == "__main__":
#     freeze_support()
#     try:
#         genai_chain = initialize_chain()
#         question = "What should I do if I have a headache?"
#         answer = generate_answer(InputQA(question=question), genai_chain)
#         print(f"Answer: {answer['answer']}")
#     except Exception as e:
#         print(f"Application error: {str(e)}")
