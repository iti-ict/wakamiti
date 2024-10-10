import requests
import json
from requests.auth import HTTPBasicAuth

# Configuración
JIRA_URL = "https://xxxxxxxxxxxxxxx.atlassian.net"
API_TOKEN = "ATATT3xxxxxxxxxxxxxxxxxxxxx=xxxxxxxx"
EMAIL = ""
PROJECT_KEY = "WAK"

# Crear un Test Set en Jira Cloud
def create_test_set(jira_url, email, api_token, project_key, test_set_name, description):
    url = f"{jira_url}/rest/api/3/issue"
    headers = {
        "Content-Type": "application/json"
    }
    auth = HTTPBasicAuth(email, api_token)
    payload = {
        "fields": {
            "project": {
                "key": project_key
            },
            "summary": test_set_name,
            "description": {
                "type": "doc",
                "version": 1,
                "content": [
                    {
                        "type": "paragraph",
                        "content": [
                            {
                                "text": description,
                                "type": "text"
                            }
                        ]
                    }
                ]
            },
            "issuetype": {
                "name": "Test Set"
            }
        }
    }
    
    response = requests.post(url, headers=headers, auth=auth, data=json.dumps(payload))
    
    if response.status_code == 201:
        print("Test Set created successfully.")
        return response.json()
    else:
        print(f"Failed to create Test Set. HTTP error code: {response.status_code}")
        try:
            print("Response JSON:", response.json())
        except json.JSONDecodeError:
            print("Response content:", response.text)
        return None

# Ejemplo de uso
# Crear Test Set
test_set_response = create_test_set(JIRA_URL, EMAIL, API_TOKEN, PROJECT_KEY, "New Test Set", "This is a description for the new test set.")

if test_set_response:
    print("Test Set Key:", test_set_response['key'])
