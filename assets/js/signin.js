async function signin() {
    let email    = document.getElementById("email-field").value
    let password = document.getElementById("password-field").value

    const response = await fetch('/signin', {
        method: 'POST',
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
    })

    if (!response.ok) {
        throw new Error("Network response was not ok " + response.statusText);
    }

    window.location.href = "/"
}