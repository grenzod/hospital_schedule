from typing import Dict, List
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from utils.async_utils import async_retry
import numpy as np
from GRAPH.build_graph import router_workflow
from GRAPH.test_Graph import generate_response
from RAG_PDF.src.rag.main import InputQA, OutputQA
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.sequence import pad_sequences
import numpy as np
import json

app = FastAPI(
    title="Hospital Chatbot",
    description="Endpoints for a hospital system graph RAG chatbot",
)

origins = ["http://localhost:4200"]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,          
    allow_credentials=True,
    allow_methods=["*"],            
    allow_headers=["*"],            
)

model = load_model("./MODEL/model_medium_V2.keras")
with open('./MODEL/word_to_idx.json', 'r', encoding='utf-8') as f:
    word_to_idx = json.load(f)
with open('./MODEL/idx_to_word.json', 'r', encoding='utf-8') as f:
    idx_to_word = {int(k):v for k,v in json.load(f).items()}
MAXLEN = 21

@app.get("/")
async def get_status():
    return {"status": "running"}

# @async_retry(max_retries=10, delay=1)
# async def invoke_agent_with_retry(query: str):
#     return await router_workflow.ainvoke({"input": query})
#
# @app.post("/hospital-rag-agent")
# async def query_hospital_agent(query: InputQA) -> str:
#     try:
#         query_response = await invoke_agent_with_retry(query.question)
#         return query_response["output"]
#     except Exception as e:
#         return {"input": query.question, "output": str(e), "intermediate_steps": []}
    
@async_retry(max_retries=10, delay=1)
async def invoke_agent_with_retry(query: List[Dict[str, str]]):
    return generate_response(query)

@app.post("/hospital-rag-agent")
async def query_hospital_agent(query: List[Dict[str, str]]) -> str:
    try:
        query_response = await invoke_agent_with_retry(query)
        return query_response
    except Exception as e:
        return {"input": query, "output": str(e), "intermediate_steps": []}

# Gợi ý từ tiếp theo    
def generate_desc(seed_text: str, next_words: int) -> str:
    in_text = seed_text
    for _ in range(next_words):
        sequence = [word_to_idx[w] for w in in_text.split() if w in word_to_idx]
        sequence = pad_sequences([sequence], maxlen=MAXLEN)[0]
        yhat = model.predict(np.array([sequence]), verbose=0)
        predicted_index = np.argmax(yhat)
        output_word = idx_to_word.get(predicted_index, "")
        if not output_word:
            break
        in_text += " " + output_word
    return in_text

@app.websocket("/ws/predict")
async def websocket_predict(ws: WebSocket):
    await ws.accept()
    try:
        while True:
            seed_text = await ws.receive_text()
            new_text = generate_desc(seed_text, 1)
            token = new_text[len(seed_text):].strip()
            await ws.send_json({
                "token": token,
                "full_text": new_text
            })
    except WebSocketDisconnect:
        print("Client disconnected")
