import requests
import json
from requests.auth import HTTPBasicAuth

# Configuración
JIRA_URL = "https://xxxxxxxxxxxxxxx.atlassian.net"
XRAY_URL = "https://xray.cloud.getxray.app"
API_TOKEN = "ATATT3xxxxxxxxxxxxxxxxxxxxx=xxxxxxxx"
EMAIL = ""
PROJECT_KEY = "WAK"  # Clave del proyecto

# Obtener JWT para Xray
def get_jwt():
    auth_payload = {
        "client_id": "",
        "client_secret": ""
    }
    response = requests.post(f"{XRAY_URL}/api/v2/authenticate", headers={"Content-Type": "application/json"}, data=json.dumps(auth_payload))
    
    if response.status_code == 200:
        print("Authentication successful.")
        return response.text.strip('"')  # El token JWT
    else:
        print(f"Failed to authenticate. HTTP error code: {response.status_code}")
        print("Response:", response.json())
        return None

# Crear un Test Plan en Jira Cloud
def create_test_plan(jira_url, email, api_token, project_key, test_plan_name, description):
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
            "summary": test_plan_name,
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
                "name": "Test Plan"
            }
        }
    }
    
    response = requests.post(url, headers=headers, auth=auth, data=json.dumps(payload))
    
    if response.status_code == 201:
        print("Test Plan created successfully.")
        return response.json()
    else:
        print(f"Failed to create Test Plan. HTTP error code: {response.status_code}")
        try:
            print("Response JSON:", response.json())
        except json.JSONDecodeError:
            print("Response content:", response.text)
        return None

# Ejemplo de uso
jwt = get_jwt()
if jwt:
    # Crear Test Plan
    test_plan_response = create_test_plan(JIRA_URL, EMAIL, API_TOKEN, PROJECT_KEY, "New Test Plan", "This is a description for the new test plan.")

    if test_plan_response:
        print("Test Plan Key:", test_plan_response['key'])
