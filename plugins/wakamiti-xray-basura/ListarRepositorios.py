import requests
import json
from requests.auth import HTTPBasicAuth

# Configuración
JIRA_URL = "https://xxxxxxxxxxxxxxx.atlassian.net"
XRAY_URL = "https://xray.cloud.getxray.app"
API_TOKEN = "ATATT3XXXXXXXXXXXXXXXXXXX=XXXXXXXX"
EMAIL = ""
PROJECT_KEY = "WAK"
CLIENT_ID = ""
CLIENT_SECRET = ""

# Obtener JWT para Xray
def get_jwt():
    auth_payload = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    response = requests.post(f"{XRAY_URL}/api/v2/authenticate", headers={"Content-Type": "application/json"}, data=json.dumps(auth_payload))
    
    if response.status_code == 200:
        print("Authentication successful.")
        return response.text.strip('"')  # El token JWT
    else:
        print(f"Failed to authenticate. HTTP error code: {response.status_code}")
        print("Response:", response.json())
        return None

# Crear una carpeta en el Test Repository
def create_test_repository_folder(jira_url, email, api_token, project_key, folder_name, parent_folder_id):
    url = f"{jira_url}/rest/raven/1.0/api/testrepository/{project_key}/folders/{parent_folder_id}"
    headers = {
        "Content-Type": "application/json"
    }
    auth = HTTPBasicAuth(email, api_token)
    payload = {
        "name": folder_name
    }
    
    response = requests.post(url, headers=headers, auth=auth, data=json.dumps(payload))
    
    if response.status_code == 201:
        print("Folder created successfully.")
        return response.json()
    else:
        print(f"Failed to create folder. HTTP error code: {response.status_code}")
        try:
            print("Response JSON:", response.json())
        except json.JSONDecodeError:
            print("Response content:", response.text)
        return None

# Main
def main():
    # Obtener el token JWT
    jwt = get_jwt()
    if jwt:
        # Crear la carpeta en el Test Repository
        parent_folder_id = -1  # Usamos -1 como ejemplo, cambiar según sea necesario
        folder_name = "Test Repository Name"
        folder_response = create_test_repository_folder(JIRA_URL, EMAIL, API_TOKEN, PROJECT_KEY, folder_name, parent_folder_id)
        
        if folder_response:
            print("Folder ID:", folder_response['id'])

if __name__ == "__main__":
    main()
