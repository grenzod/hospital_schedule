import os
os.environ['KMP_DUPLICATE_LIB_OK'] = 'TRUE'
os.environ['MKL_THREADING_LAYER'] = 'GNU'
os.environ['OMP_NUM_THREADS'] = '1'
from typing import Optional, Union
from langchain_chroma import Chroma
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.docstore import InMemoryDocstore 
import faiss
import numpy as np
import multiprocessing
from tqdm import tqdm

class VectorDB:
    @staticmethod
    def get_num_cpu():
        return min(3, multiprocessing.cpu_count())

    def __init__(
            self, 
            document:Optional[list] = None, 
            vector_db: Union[Chroma, FAISS] = FAISS, 
            embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2",
            batch_size: int = 128,
            persist_directory: Optional[str] = None,
            nlist: int = 50  # Số lượng cụm cho IndexIVFFlat
            ) -> None:
        self.vector_db = vector_db
        current_dir = os.path.dirname(os.path.abspath(__file__))
        self.persist_directory = persist_directory
        if not self.persist_directory:
            current_dir = os.path.dirname(os.path.abspath(__file__))
            self.persist_directory = os.path.join(current_dir, 'vector_db')

        faiss.omp_set_num_threads(VectorDB.get_num_cpu()) 
        self.nlist = nlist  

        self.embedding = HuggingFaceEmbeddings(
            model_name=embedding_model,
            model_kwargs={'device': 'cpu'},
            encode_kwargs={
                'normalize_embeddings': True,  
                'batch_size': batch_size,             
            }
        )
        
        if os.path.isdir(self.persist_directory) and os.listdir(self.persist_directory):
            self.db = FAISS.load_local(
                self.persist_directory,
                embeddings=self.embedding,
                allow_dangerous_deserialization=True
            )
        elif document:
            self.db = self._build_db_(document)
        else:
            self.db = None

    def _embed_document(self, doc):
        return self.embedding.embed_query(doc.page_content)

    def _build_db_(self, documents):
        # Tạo embedding cho tài liệu
        embeddings = []
        with multiprocessing.Pool(processes=VectorDB.get_num_cpu()) as pool:
            embeddings = list(tqdm(
                pool.imap(  # Using imap instead of map for progress tracking
                    self._embed_document,  # Use class method instead of lambda
                    documents
                ),
            total=len(documents),
            desc="Creating embeddings",
            unit="doc"  # Shows progress per document
        ))
        embeddings = np.array(embeddings, dtype=np.float32)

        # Tạo chỉ mục IndexIVFFlat
        embedding_dim = embeddings.shape[1]  # Kích thước của vector embedding
        quantizer = faiss.IndexFlatL2(embedding_dim)  # Quantizer cho IndexIVFFlat
        index = faiss.IndexIVFFlat(quantizer, embedding_dim, self.nlist, faiss.METRIC_L2)
        index.nprobe = 10  # Số cụm sẽ tìm kiếm (tăng tốc độ nhưng có thể giảm độ chính xác)

        # Huấn luyện chỉ mục và thêm vector
        index.train(embeddings)
        index.add(embeddings)

        # Tạo FAISS vector store với chỉ mục đã tối ưu
        doc_dict = {i: doc for i, doc in enumerate(documents)}
        self.db = FAISS(
            embedding_function=self.embedding,
            index=index,
            docstore=InMemoryDocstore(doc_dict), 
            index_to_docstore_id={i: i for i in range(len(documents))}
        )
        if self.persist_directory:
            self.db.save_local(self.persist_directory)
        return self.db

    def get_retriever(self, 
                      search_type: str = "similarity",
                      search_kwargs: dict = {"k": 5}):
        return self.db.as_retriever(
            search_type=search_type, 
            search_kwargs=search_kwargs
        )

    def load_db(self):
        if self.persist_directory:
            self.db = FAISS.load_local(
                folder_path=self.persist_directory,
                embeddings=self.embedding,
                allow_dangerous_deserialization=True  # Add this parameter
            )
        return self.db

    def persist(self):
        if hasattr(self.db, "save_local"):
            self.db.save_local(self.persist_directory)
