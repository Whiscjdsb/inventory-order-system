const queryButton = document.getElementById("queryButton");
const productIdInput = document.getElementById("productId");
const result = document.getElementById("result");
const listButton = document.getElementById("listButton");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const loginButton = document.getElementById("loginButton");
const loginResult = document.getElementById("loginResult");
const productNameInput = document.getElementById("productName");
const productPriceInput = document.getElementById("productPrice");
const productStockInput = document.getElementById("productStock");
const createButton = document.getElementById("createButton");
const createResult = document.getElementById("createResult");
const logoutButton = document.getElementById("logoutButton");

let authToken = sessionStorage.getItem("authToken") || "";



function createProductHtml(product) {
    return `
        <div class="product-item">
            <h2>${product.name}</h2>
            <p>价格：${product.price} 元</p>
            <p>库存：${product.stock}</p>
            <p>状态：${product.statusText}</p>
        </div>
    `;
}

if (authToken !== "") {
    loginResult.textContent = "已读取登录信息";
}
queryButton.addEventListener("click", function () {
    const productId = Number(productIdInput.value);

    if (authToken === "") {
        result.textContent = "请先登录";
        return;
    }

    if (productId < 1) {
        result.textContent = "商品 ID 必须大于等于 1";
        return;
    }

    fetch("http://localhost:8080/api/products/" + productId, {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + authToken
        }
    })
        .then(function (response) {
            if (response.status === 401) {
                authToken = "";
                sessionStorage.removeItem("authToken");
                loginResult.textContent = "登录已过期，请重新登录";
                return null;
            }

            return response.json();
        })
        .then(function (responseData) {
            if (responseData === null) {
                return;
            }

            if (responseData.code !== 200) {
                result.textContent = responseData.message;
                return;
            }

            const product = responseData.data;

            result.innerHTML = createProductHtml(product);
        })
        .catch(function (error) {
            result.textContent = "商品查询失败";
            console.error(error);
        });
});

listButton.addEventListener("click", function () {
    if (authToken === "") {
        result.textContent = "请先登录";
        return;
    }

    fetch("http://localhost:8080/api/products", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + authToken
        }
    })
        .then(function (response) {
            if (response.status === 401) {
                authToken = "";
                sessionStorage.removeItem("authToken");
                loginResult.textContent = "登录已过期，请重新登录";
                return null;
            }

            return response.json();
        })
        .then(function (responseData) {
            if (responseData === null) {
                return;
            }

            if (responseData.code !== 200) {
                result.textContent = responseData.message;
                return;
            }

            const products = responseData.data;
            const productHtmlList = products.map(function (product) {
                return createProductHtml(product);
            });

            if (productHtmlList.length === 0) {
                result.textContent = "暂无商品";
                return;
            }

            result.innerHTML = productHtmlList.join("");
        })
        .catch(function (error) {
            result.textContent = "商品列表查询失败";
            console.error(error);
        });
});
fetch("http://localhost:8080/actuator/health")
    .then(function (response) {
        return response.json();
    })
    .then(function (data) {
        console.log("后端状态：", data);
    })
    .catch(function (error) {
        console.error("请求失败：", error);
    });
loginButton.addEventListener("click", function () {
    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
        .then(function (response) {
            return response.json();
        })
        .then(function (responseData) {
            if (responseData.code !== 200) {
                loginResult.textContent = responseData.message;
                return;
            }

            authToken = responseData.data.token;
            sessionStorage.setItem("authToken", authToken);
            loginResult.textContent = "登录成功";
        })
        .catch(function (error) {
            loginResult.textContent = "无法连接后端";
            console.error(error);
        });
});
createButton.addEventListener("click", function () {
    if (authToken === "") {
        createResult.textContent = "请先登录";
        return;
    }

    const name = productNameInput.value.trim();
    const price = Number(productPriceInput.value);
    const stock = Number(productStockInput.value);

    if (name === "") {
        createResult.textContent = "商品名称不能为空";
        return;
    }

    if (price <= 0) {
        createResult.textContent = "商品价格必须大于 0";
        return;
    }

    if (stock < 0) {
        createResult.textContent = "库存不能小于 0";
        return;
    }

    fetch("http://localhost:8080/api/products", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + authToken
        },
        body: JSON.stringify({
            name: name,
            price: price,
            stock: stock
        })
    })
        .then(function (response) {
            if (response.status === 401) {
                authToken = "";
                sessionStorage.removeItem("authToken");
                loginResult.textContent = "登录已过期，请重新登录";
                return null;
            }

            return response.json();
        })
        .then(function (responseData) {
            if (responseData === null) {
                return;
            }

            if (responseData.code !== 200) {
                createResult.textContent = responseData.message;
                return;
            }

            const createdProduct = responseData.data;

            createResult.textContent =
                "新增成功，商品 ID：" + createdProduct.id;

            productIdInput.value = createdProduct.id;
        })
        .catch(function (error) {
            createResult.textContent = "新增商品失败";
            console.error(error);
        });
});
logoutButton.addEventListener("click", function () {
    authToken = "";
    sessionStorage.removeItem("authToken");

    passwordInput.value = "";
    loginResult.textContent = "已退出登录";
    result.textContent = "请重新登录后查询商品";
});