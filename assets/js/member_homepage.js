async function signout() {

    const resp = await fetch("/private/signout", {
        method: 'PUT'
    })

    if (!resp.ok) {
        return alert("failed to sign out")
    }

    return window.location.href = "/"

}