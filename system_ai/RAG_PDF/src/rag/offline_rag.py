import re
from langchain import hub
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import HumanMessage, AIMessage

class Str_OutputParser(StrOutputParser):
	def __init__(self) -> None:
		super().__init__()

	def parse(self, text: str) -> str:
		return self.extract_answer(text)

	def extract_answer(
		self,
		text_response: str,
		pattern: str = r"Answer:\s*(.*)"
	) -> str:
		match = re.search(pattern, text_response, re.DOTALL)
		if match:
			answer_text = match.group(1).strip()
			return answer_text
		else:
			return text_response

# class Offline_RAG:
# 	def __init__(self, llm) -> None:
# 		self.llm = llm
# 		self.prompt = hub.pull("rlm/rag-prompt")
# 		self.str_parser = Str_OutputParser()

# 	def get_chain(self, retriever):
# 		input_data = {
# 			"context": retriever | self.format_docs,
# 			"question": RunnablePassthrough()
# 		}
# 		rag_chain = (
# 			input_data
# 			| self.prompt
# 			| self.llm
# 			| self.str_parser
# 		)
# 		return rag_chain

# 	def format_docs(self, docs):
# 		return "\n\n".join(doc.page_content for doc in docs)

class Offline_RAG:
    def __init__(self, llm) -> None:
        self.llm = llm
        self.prompt = hub.pull("rlm/rag-prompt")  
        self.str_parser = Str_OutputParser()

    def get_chain(self, retriever):
        def format_chat_history(chat_messages):
            if isinstance(chat_messages, list):
                if all(isinstance(m, dict) and "role" in m and "content" in m for m in chat_messages):
                    # Format từ [{"role": "user", "content": "..."}]
                    history = []
                    question = ""
                    for msg in chat_messages:
                        if msg["role"] == "user":
                            history.append(f"Human: {msg['content']}")
                            question = msg['content']  # Lấy câu hỏi cuối cùng
                        else:
                            history.append(f"Assistant: {msg['content']}")
                    return {"chat_history": "\n".join(history), "question": question}
                else:
                    # Format từ list LangChain messages
                    history = []
                    question = ""
                    for msg in chat_messages:
                        if isinstance(msg, HumanMessage):
                            history.append(f"Human: {msg.content}")
                            question = msg.content
                        elif isinstance(msg, AIMessage):
                            history.append(f"Assistant: {msg.content}")
                    return {"chat_history": "\n".join(history), "question": question}
            return {"chat_history": "", "question": str(chat_messages)}

        def prepare_inputs(messages):
            formatted = format_chat_history(messages)
            return {
                "context": retriever.invoke(formatted["question"]),
                "chat_history": formatted["chat_history"],
                "question": formatted["question"]
            }

        rag_chain = (
            RunnablePassthrough() 
            | prepare_inputs
            | self.prompt
            | self.llm
            | self.str_parser
        )
        return rag_chain
	