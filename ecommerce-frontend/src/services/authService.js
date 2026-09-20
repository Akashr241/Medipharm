import API from "./api";

// ==========================================
// REGISTER
// ==========================================

export const registerUser = async (userData) => {

    console.log("");
    console.log("==========================================");
    console.log("        REGISTER REQUEST STARTED");
    console.log("==========================================");

    console.log("Register Data:", userData);

    try {

        const response = await API.post(
            "/auth/register",
            userData
        );

        console.log("========== REGISTER SUCCESS ==========");
        console.log("Status:", response.status);
        console.log("Response:", response.data);

        return response.data;

    } catch (error) {

        console.error("========== REGISTER FAILED ==========");

        console.error("Error:", error);
        console.error("Message:", error.message);
        console.error("Code:", error.code);

        if (error.response) {

            console.error("Status:", error.response.status);
            console.error("Response:", error.response.data);
            console.error("Headers:", error.response.headers);

        } else if (error.request) {

            console.error(
                "Request was sent, but NO RESPONSE was received."
            );

            console.error("Request:", error.request);

        } else {

            console.error(
                "Request could not be created."
            );
        }

        throw error;
    }
};


// ==========================================
// GOOGLE LOGIN
// ==========================================

export const loginWithGoogle = () => {

    const apiUrl =
        process.env.REACT_APP_API_URL;

    console.log("");
    console.log("==========================================");
    console.log("        GOOGLE LOGIN STARTED");
    console.log("==========================================");

    console.log("API URL:", apiUrl);

    const googleUrl =
        `${apiUrl}/oauth2/authorization/google`;

    console.log("Google Login URL:", googleUrl);

    window.location.href = googleUrl;
};


// ==========================================
// NORMAL LOGIN
// ==========================================

export const loginUser = async (credentials) => {

    console.log("");
    console.log("==========================================");
    console.log("          LOGIN REQUEST STARTED");
    console.log("==========================================");

    console.log("Login Email:", credentials.email);

    try {

        const response = await API.post(
            "/auth/login",
            credentials
        );

        console.log("");
        console.log("========== LOGIN SUCCESS ==========");

        console.log("Status:", response.status);
        console.log("Response:", response.data);

        console.log("==================================");
        console.log("");

        return response.data;

    } catch (error) {

        console.error("");
        console.error("========== LOGIN FAILED ==========");

        console.error("Error:", error);
        console.error("Message:", error.message);
        console.error("Code:", error.code);

        // --------------------------------------
        // SERVER RETURNED A RESPONSE
        // --------------------------------------

        if (error.response) {

            console.error("Server Response Received");

            console.error(
                "Status:",
                error.response.status
            );

            console.error(
                "Response Data:",
                error.response.data
            );

            console.error(
                "Response Headers:",
                error.response.headers
            );
        }

        // --------------------------------------
        // REQUEST SENT BUT NO RESPONSE
        // --------------------------------------

        else if (error.request) {

            console.error(
                "Request was sent but NO RESPONSE was received."
            );

            console.error(
                "Request Object:",
                error.request
            );
        }

        // --------------------------------------
        // REQUEST NOT CREATED
        // --------------------------------------

        else {

            console.error(
                "Axios could not create the request."
            );
        }

        console.error("==================================");
        console.error("");

        throw error;
    }
};