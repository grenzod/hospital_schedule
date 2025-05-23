import dotenv
from langchain_community.utilities import SQLDatabase
from langchain.chat_models import init_chat_model
from langchain_community.tools.sql_database.tool import QuerySQLDatabaseTool
from langchain.chains import RetrievalQA
from langchain.prompts import (
    PromptTemplate,
    SystemMessagePromptTemplate,
    HumanMessagePromptTemplate,
    ChatPromptTemplate,
)

dotenv.load_dotenv()
mysql_url = "mysql+mysqlconnector://root:175003@localhost:3306/hospital"
db = SQLDatabase.from_uri(mysql_url)
llm = init_chat_model("llama3-70b-8192", model_provider="groq")

query = """
    SELECT comment
    FROM reviews
    WHERE comment IS NOT NULL AND comment != ''
    ORDER BY created_at DESC
    LIMIT 10
    """
execute_query_tool = QuerySQLDatabaseTool(db=db)
results = execute_query_tool.invoke(query)

def process_results(results_string: str) -> list:
    """Process the results string into a list of comments"""
    clean_str = results_string.strip('[]')
    rows = clean_str.split('), (')
    
    comments = []
    for row in rows:
        clean_row = row.strip("(')")
        if clean_row:
            comments.append(clean_row)
    
    return comments

comments = process_results(results)

review_template = """Answer the question about doctor reviews based on the following information. Be brief and accurate.
If you don't know an answer, say you don't know.
{context}
"""

review_system_prompt = SystemMessagePromptTemplate(
    prompt=PromptTemplate(input_variables=["context"], template=review_template)
)

review_human_prompt = HumanMessagePromptTemplate(
    prompt=PromptTemplate(input_variables=["question"], template="{question}")
)

review_prompt = ChatPromptTemplate(
    input_variables=["context", "question"],
    messages=[review_system_prompt, review_human_prompt]
)

from langchain.schema import Document
from langchain_community.retrievers import TFIDFRetriever 

documents = [Document(page_content=comment) for comment in comments]
retriever = TFIDFRetriever.from_documents(documents)

reviews_chain = RetrievalQA.from_chain_type(
    llm=llm,
    chain_type="stuff",
    retriever=retriever,
    chain_type_kwargs={"prompt": review_prompt}
)

def get_review_response(question: str) -> str:
    try:
        vector_response = reviews_chain.invoke(question)
        return vector_response
        
    except Exception as e:
        print(f"Error: {str(e)}")
        return "I cannot find any related reviews."
    