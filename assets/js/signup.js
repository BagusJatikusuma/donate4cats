async function signup() {
    const name      = document.getElementById("name-field").value
    const email     = document.getElementById("email-field").value
    const password  = document.getElementById("password-field").value

    const response = await fetch("/signup", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name, email, password }),
    })

    if (!response.ok) {
        return alert("signup failed")
    }

    return window.location.href = "/"
}