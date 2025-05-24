import os
import sys
from pathlib import Path
os.environ["TOKENIZERS_PARALLELISM"] = "false"
from multiprocessing import freeze_support
from langchain_core.messages import SystemMessage
PROJECT_ROOT = Path(__file__).parent
DATA_DIR = PROJECT_ROOT / "data_source" / "generative_ai"
sys.path.append(str(PROJECT_ROOT))
from src.base.llm_model import get_groq_llm
from src.rag.main import build_rag_chain

def initialize_chain():
    llm = get_groq_llm(temperature=0.9)
    chain = build_rag_chain(llm, data_dir=str(DATA_DIR), data_type="pdf")
    return chain

def generate_answer(messages: list) -> str:
    try:
        chain = initialize_chain()
        answer = chain.invoke(messages)
        return {"answer": answer}

    except Exception as e:
        print(f"Error in generate_answer: {str(e)}")
        raise

def get_response(messages: list) -> str: 
    try:
        messages = [
            SystemMessage(content="""You are a specialized medical AI assistant with expertise in healthcare. When answering questions:
                - Focus on medical information from reliable documents
                - Explain medical terms in simple Vietnamese
                - Be clear and precise with medical advice
                - Include relevant symptoms, treatments, and precautions
                - Maintain medical accuracy while being easy to understand
                - If unsure, acknowledge limitations and suggest consulting a doctor
                - Keep patient safety as the top priority
                - Structure answers with clear sections when applicable
                - Use proper medical terminology alongside lay explanations
                Remember to maintain conversation context and answer in Vietnamese."""
            ),
            *messages 
        ]
            
        response = generate_answer(messages)
        return response["answer"]
    except Exception as e:
        print(f"Error generating answer: {str(e)}")
        return "Xin lỗi, tôi gặp lỗi khi tìm kiếm thông tin y tế. Vui lòng thử lại."

if __name__ == "__main__":
    freeze_support()
    try:
        genai_chain = initialize_chain()
        question = [{"role": "user","content": "What should I do if I have a headache?"}]

        answer = get_response(question)
        print(f"Answer: {answer}")
    except Exception as e:
        print(f"Application error: {str(e)}")