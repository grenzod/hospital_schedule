import os
import wget

file_links = [
    {
        "title": "Guide to Infection Prevention for Outpatient Settings",
        "url": "https://www.cdc.gov/infection-control/media/pdfs/Outpatient-Guide-508.pdf"
    },
    {
        "title": "Sexually Transmitted Infections Treatment Guidelines, 2021",
        "url": "https://www.cdc.gov/std/treatment-guidelines/STI-Guidelines-2021.pdf"
    },
    {
        "title": "Infection Control in Healthcare Personnel: Epidemiology and Diagnosis",
        "url": "extension://mjdgandcagmikhlbjnilkmfnjeamfikk/https://www.cdc.gov/infection-control/media/pdfs/Guideline-IC-HCP-H.pdf"
    },
    {
        "title": "Infection Control in Healthcare Personnel: Epidemiology and Diagnosis",
        "url": "https://www.cdc.gov/infection-control/media/pdfs/Guideline-IC-HCP-H.pdf"
    },
    {
        "title": "Infection Control in Healthcare Personnel: Epidemiology and Diagnosis",
        "url": "https://www.cdc.gov/infection-control/media/pdfs/Guideline-IC-HCP-H.pdf"
    },
    {
        "title": "Infection Control in Healthcare Personnel: Epidemiology and Diagnosis",
        "url": "https://www.cdc.gov/infection-control/media/pdfs/Guideline-IC-HCP-H.pdf"
    }
]

def is_exist(file_link):
    """Check if file already exists"""
    current_dir = os.path.dirname(os.path.abspath(__file__))
    return os.path.exists(os.path.join(current_dir, f"{file_link['title']}.pdf"))

def download_papers():
    """Download papers if they don't exist"""
    current_dir = os.path.dirname(os.path.abspath(__file__))
    for file_link in file_links:
        if not is_exist(file_link):
            print(f"Downloading {file_link['title']}...")
            wget.download(
                file_link["url"].strip(),
                out=os.path.join(current_dir, f"{file_link['title'].strip()}.pdf")
            )
            print("\nDownload completed!")
        else:
            print(f"File {file_link['title']} already exists.")

if __name__ == "__main__":
    download_papers()