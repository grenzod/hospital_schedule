import re

def extract_answer(
    text_response: str,
    pattern: str = r"Answer:\s*(.*)"
) -> str:
    """
    Extracts the answer from the text response using a regex pattern.
    
    Args:
        text_response (str): The text response to extract the answer from.
        pattern (str): The regex pattern to use for extraction. Defaults to r"Answer:\s*(.*)".
    
    Returns:
        str: The extracted answer or the original text response if no match is found.
    """
    match = re.search(pattern, text_response, re.DOTALL)
    if match:
        answer_text = match.group(1).strip()
        return answer_text
    else:
        return "Not found Answer"