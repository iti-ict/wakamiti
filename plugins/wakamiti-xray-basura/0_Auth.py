import requests
import json

# Configuración
AUTH_URL = "https://xray.cloud.getxray.app/api/v2/authenticate"
CLIENT_ID = ""
CLIENT_SECRET = ""

# Autenticación para obtener el token JWT
def get_jwt():
    auth_payload = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    response = requests.post(AUTH_URL, headers={"Content-Type": "application/json"}, data=json.dumps(auth_payload))
    
    if response.status_code == 200:
        print("Authentication successful.")
        print("JWT Token:", response.text.strip('"'))
        return response.text.strip('"')  # El token JWT
    else:
        print(f"Failed to authenticate. HTTP error code: {response.status_code}")
        print("Response:", response.json())
        return None

# Ejemplo de uso
jwt = get_jwt()
