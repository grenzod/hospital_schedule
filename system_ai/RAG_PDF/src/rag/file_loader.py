import os
from typing import Union, List, Literal
import glob
from tqdm import tqdm
import multiprocessing
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.documents import Document
import pickle
import fitz  
from langchain_core.documents import Document
import re

def remove_non_utf8_chracters(text: str) -> str:
    return ''.join(c for c in text if ord(c) < 128)

def load_pdf(file_path):
    try:
        file_size = os.path.getsize(file_path) / (1024 * 1024)
        print(f"Processing {file_path} ({file_size:.2f} MB)")
        
        docs = []
        doc = fitz.open(file_path)
        for page_num, page in enumerate(doc):
            text = page.get_text()
            if text:
                text = clean_pdf_text(text)
                docs.append(Document(
                    page_content=text,
                    metadata={"source": file_path, "page": page_num + 1}
                ))
        doc.close()
        return docs
    except Exception as e:
        print(f"Error loading PDF {file_path}: {str(e)}")
        return []

def clean_pdf_text(text):
    if not text:
        return ""
    text = ''.join(c for c in text if ord(c) < 128)
    text = re.sub(r'\x0c', '\n', text)
    text = re.sub(r'\s+', ' ', text)
    text = re.sub(r'\n\s*\n', '\n', text)
    return text.strip()

def get_num_cpu():
    return multiprocessing.cpu_count()

class BaseLoader:
    def __init__(self) -> None:
        self.num_processes = get_num_cpu()

    def __call__(self, files: List[str], **kwargs):
        pass

class PDFLoader(BaseLoader):
    def __init__(self) -> None:
        super().__init__()

    def __call__(self, files: List[str], **kwargs):
        num_process = min(self.num_processes, kwargs["workers"])

        chunksize = max(1, len(files) // (num_process * 2))

        with multiprocessing.Pool(processes=num_process) as pool:
            doc_loaded = []
            total_files = len(files)

            with tqdm(total=total_files, desc="Loading PDFs", unit="file") as pbar:
                for result in pool.imap_unordered(load_pdf, files, chunksize=chunksize):
                    doc_loaded.extend(result)
                    pbar.update(1)
            return doc_loaded

class TextSplitter:
    def __init__(
            self,
            separators: List[str] = ['\n\n', '\n', ' ', ''],
            chunk_size: int = 500,
            chunk_overlap: int = 50
    ) -> None:
        self.splitter = RecursiveCharacterTextSplitter(
            separators=separators,
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap,
        )

    def __call__(self, documents):
        return self.splitter.split_documents(documents)

class Loader:
    def __init__(
            self,
            file_type: str = "pdf",
            split_kwargs: dict = {
                "chunk_size": 500,
                "chunk_overlap": 50
            }
    ) -> None:
        assert file_type in ["pdf"], "file_type must be pdf"
        self.file_type = file_type
        if file_type == "pdf":
            self.doc_loader = PDFLoader()
        else:
            raise ValueError("file_type must be pdf")

        self.doc_splitter = TextSplitter(**split_kwargs)

    def load(self, pdf_files: Union[str, List[str]], workers: int = 1):
        if isinstance(pdf_files, str):
            pdf_files = [pdf_files]
        doc_loaded = self.doc_loader(pdf_files, workers=workers)
        doc_split = self.doc_splitter(doc_loaded)
        return doc_split

    def load_dir(self, dir_path: str, workers: int = 1):
        if self.file_type == "pdf":
            files = glob.glob(f"{dir_path}/*.pdf")
            assert len(files) > 0, f"No {self.file_type} files found in {dir_path}"
        else:
            raise ValueError("file_type must be pdf")

        return self.load(files, workers=workers)
    
    def save_processed_docs(self, documents, output_file="processed_docs.pkl"):
        with open(output_file, "wb") as f:
            pickle.dump(documents, f)
        print(f"Saved {len(documents)} processed documents to {output_file}")
    
    def load_processed_docs(self, input_file="processed_docs.pkl"):
        if not os.path.exists(input_file):
            raise FileNotFoundError(f"File {input_file} not found")
            
        with open(input_file, "rb") as f:
            documents = pickle.load(f)
        print(f"Loaded {len(documents)} processed documents from {input_file}")
        return documents
