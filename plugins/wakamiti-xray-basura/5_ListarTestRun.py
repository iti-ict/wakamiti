import requests
import json

# Configuración
XRAY_CLOUD_URL = "https://xray.cloud.getxray.app"
JIRA_URL = "https://xxxxxxxxxxxxxxx.atlassian.net"  # URL de tu instancia de Jira
CLIENT_ID = ""
CLIENT_SECRET = ""

# Obtener JWT para Xray
def get_jwt():
    auth_payload = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    response = requests.post(f"{XRAY_CLOUD_URL}/api/v2/authenticate", headers={"Content-Type": "application/json"}, data=json.dumps(auth_payload))
    
    if response.status_code == 200:
        print("Authentication successful.")
        return response.text.strip('"')  # El token JWT
    else:
        print(f"Failed to authenticate. HTTP error code: {response.status_code}")
        print("Response:", response.json())
        return None

# Obtener los Test Runs
def get_test_runs(jwt_token):
    base_url = f"{JIRA_URL}/rest/raven/2.0/testruns"
    
    # Parámetros de la solicitud
    params = {
        "savedFilterId": "",
        "limit": 10,
        "page": 1
    }
    
    # Encabezados de la solicitud
    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "application/json"
    }
    
    # Realizar la solicitud GET
    response = requests.get(base_url, headers=headers, params=params)
    
    # Verificar el estado de la respuesta y mostrar los datos
    if response.status_code == 200:
        data = response.json()
        print(data)
    else:
        print(f"Error: {response.status_code}")
        print(response.text)

# Ejecución principal
if __name__ == "__main__":
    jwt_token = get_jwt()
    if jwt_token:
        get_test_runs(jwt_token)
