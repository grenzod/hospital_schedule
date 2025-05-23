import dotenv
from langchain_groq import ChatGroq
dotenv.load_dotenv()

def get_groq_llm(
        model_name: str = "llama3-70b-8192",  
        temperature: float = 0.7,
        max_tokens: int = 1024,
        **kwargs
    ):
    llm = ChatGroq(
        model_name=model_name,
        temperature=temperature,
        max_tokens=max_tokens,
        **kwargs
    )
    return llm
