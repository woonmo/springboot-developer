
// 글 삭제 기능
const deleteButton = document.getElementById("delete-btn");

if (deleteButton) {
    deleteButton.addEventListener("click", e => {
        let id = document.getElementById("article-id").value;
        function success() {
            alert("글이 삭제되었습니다.")
            location.replace("/articles");
        }
        function fail(err) {
            alert("글 삭제를 실패했습니다.");
            // console.log("에러: "+ err);
            location.replace("/articles");
        }
        httpRequest("DELETE", "/api/articles/" + id, null, success, fail);
    });
}// end of if (deleteButton) ---------------------


// 수정
const modifyButton = document.getElementById("modify-btn");

if (modifyButton) {
    modifyButton.addEventListener("click", e => {
        let params = new URLSearchParams(location.search);
        // 현재 페이지의 쿼리스트링을 가져옴 key-value 형태
        let id = params.get("id");

        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value,
        });

        function success() {
            alert("수정에 성공했습니다.")
            location.replace("/articles/"+ id);
        }
        function fail(err) {
            alert("수정에 실패했습니다.")
            // console.log(err.message);
            // alert(err.message);
            location.replace("/articles/"+ id);
        }

        httpRequest("PUT", "/api/articles/" + id, body, success, fail);
    });
}// end of if (modifyButton) --------------


// 등록
const createButton = document.getElementById("create-btn");
if (createButton) {
    // 등록 버튼을 클릭하면 /api/articles로 요청을 보냄
    createButton.addEventListener("click", e => {
        // fetch(`/api/articles`, {
        //     method: "POST",
        //     headers: {
        //         "Content-Type": "application/json",
        //     },
        //     body: JSON.stringify({
        //         title: document.getElementById("title").value,
        //         content: document.getElementById("content").value
        //     })
        // })
        // .then(() => {
        //     alert("Successfully created article!");
        //     location.replace(`/articles`);
        // });
        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value
        });
        function success() {
            alert("Successfully created article!");
            location.replace(`/articles`);
        }
        function fail(err) {
            alert("Failed to create article!");
            location.replace(`/articles`);
        }
        httpRequest("POST", "/api/articles", body, success, fail);
    });
}// end of if (createButton) --------------------


// 쿠키를 가져오는 함수
function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(';');
    cookie.some(function (cookie) {
        cookie = cookie.replace(" ", "");

        var dic = cookie.split('=');

        if (dic[0] === key) {
            result = dic[1];
            return true;
        }
    });
    return result;
}// end of function getCookie(key) ----------------------


// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail) {
    // console.log("Token:", localStorage.getItem("access_token"));

    fetch(url, {
        method: method,
        headers: {
            // 로컬 스토리지에서 액세스 토큰 값을 가져와 헤더에 추가
            Authorization: `Bearer ${localStorage.getItem('access_token')}`,
            "Content-Type": "application/json"
        },
        body: body
    })// fetch
        .then( response => {
            if (response.status === 200 || response.status === 201) {
                // 성공 혹은 등록에 성공
                return success();
            }// end of if (response.status === 200 || response.status === 201) -----------
            const refresh_token = getCookie('refresh_token');
            if (response.status === 401 && refresh_token) {
                // 토큰이 만료된 경우
                fetch("/api/token", {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('access_token')}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        refreshToken: getCookie('refresh_token')
                    }),
                })// fetch
                    .then(res => {
                        if (res.ok) {
                            return res.json();
                        }
                    })
                    .then(result => {
                        // 재발급이 성공하면 로컬 스토리지 값을 새로운 액세스 토큰으로 교체
                        localStorage.setItem('access_token', result.accessToken);
                        httpRequest(method, url, body, success, fail);
                    })
                    .catch(error => fail(error));
            }// end of if (response.status === 401 && refresh_token) ------------
            else {
                return fail();
            }
        })
        .catch(error => console.log("http error "+ error));
}// end of function httpRequest(method, url, body, success, fail) ---------------