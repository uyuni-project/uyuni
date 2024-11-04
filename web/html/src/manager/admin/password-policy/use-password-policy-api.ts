// Example implementation
const usePasswordPolicyApi = () => {
  const updatePolicy = (policyData) => {
    return fetch('/rhn/manager/api/admin/config/password-policy', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(policyData),
    }).then((response) => {
      console.log(response)
      if (!response.ok) {
        return response.json().then((errorData) => {
          throw errorData;
        });
      }
      return response.json();
    });
  };
  return { updatePolicy };
};

export default usePasswordPolicyApi;
