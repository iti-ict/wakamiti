import requests
import json

# Configuración
XRAY_URL = "https://xray.cloud.getxray.app"
JWT = "ATATT3xxxxxxxxxxxxxxxxxxxxx=xxxxxxxx"
TEST_EXECUTION_ID = "WAK-31"
TEST_ISSUE_ID = "WAK-7"

# Crear un Test Run en Xray
def create_test_run(xray_url, jwt, test_execution_id, test_issue_id):
    url = f"{xray_url}/api/v1/testrun"
    headers = {
        "Authorization": f"Bearer {jwt}",
        "Content-Type": "application/json"
    }
    payload = {
        "testExecIssueId": test_execution_id,
        "testIssueId": test_issue_id
    }

    print("Request URL:", url)
    print("Request Headers:", headers)
    print("Request Payload:", payload)
    
    response = requests.post(url, headers=headers, data=json.dumps(payload))
    
    print("Response Status Code:", response.status_code)
    print("Response Text:", response.text)

    if response.status_code == 200:
        print("Test Run created successfully.")
        return response.json()
    else:
        print(f"Failed to create Test Run. HTTP error code: {response.status_code}")
        try:
            response_json = response.json()
            print("Response JSON:", response_json)
        except json.JSONDecodeError:
            print("Response content is not in JSON format:", response.text)
        return None

# Ejemplo de uso
test_run_response = create_test_run(XRAY_URL, JWT, TEST_EXECUTION_ID, TEST_ISSUE_ID)
if test_run_response:
    print("Test Run ID:", test_run_response['id'])
