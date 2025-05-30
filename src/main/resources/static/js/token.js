const token = searchParam('token');


if (token) {
    localStorage.setItem("access_token", token);
}

function searchParam(key) {
    return new URLSearchParams(location.search).get(key);
}



// function getCookie(name) {
//     const value = `; ${document.cookie}`;
//     const parts = value.split(`; ${name}=`);
//     if (parts.length === 2) return parts.pop().split(';').shift();
// }
// const accessToken = getCookie('access_token');
// if (accessToken) {
//     localStorage.setItem('access_token', accessToken);
// }


// 로그아웃 시 토큰 삭제
const logoutButton = document.getElementById("logout");

if (logoutButton) {
    logoutButton.addEventListener("click", function () {
        localStorage.removeItem('access_token');
        location.href="/logout";
    })
}