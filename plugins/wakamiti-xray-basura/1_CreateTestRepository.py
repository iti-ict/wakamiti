import requests
import json

# Configuración
AUTH_URL = "https://xray.cloud.getxray.app/api/v2/authenticate"
CLIENT_ID = ""
CLIENT_SECRET = ""
PROJECT_KEY = "WAK"
FOLDER_NAME = "Test Repository Name"
PARENT_FOLDER_ID = "-1"

# Autenticación para obtener el token JWT
def get_jwt():
    auth_payload = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    response = requests.post(AUTH_URL, headers={"Content-Type": "application/json"}, data=json.dumps(auth_payload))
    
    if response.status_code == 200:
        print("Authentication successful.")
        return response.text.strip('"')
    else:
        print(f"Failed to authenticate. HTTP error code: {response.status_code}")
        print("Response:", response.json())
        return None

# Crear carpeta en el Test Repository
def create_folder(jwt_token, project_key, folder_name, parent_folder_id):
    url = f"https://xray.cloud.getxray.app/rest/raven/1.0/api/testrepository/{project_key}/folders/{parent_folder_id}"
    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "application/json"
    }
    payload = {
        "name": folder_name
    }
    response = requests.post(url, headers=headers, data=json.dumps(payload))
    
    if response.status_code == 201:
        print(f"Folder '{folder_name}' created successfully.")
        print("Response:", response.json())
    else:
        print(f"Failed to create folder. HTTP error code: {response.status_code}")
        try:
            print("Response:", response.json())
        except json.JSONDecodeError:
            print("Response content is not valid JSON")
        print("Response text:", response.text)

# Main
def main():
    jwt_token = get_jwt()
    if jwt_token:
        create_folder(jwt_token, PROJECT_KEY, FOLDER_NAME, PARENT_FOLDER_ID)

if __name__ == "__main__":
    main()
