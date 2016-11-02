# iGPSPORT Connect

> 允许第三方app连接iGPSPORT账户并上传fit文件到iGPSPORT网站


点击下载 [demo](http://my.igpsport.com/staticfile/testIgpsportAuth.apk)

### 引导用户授权

在授权Activity中防止WebView控件，导航至url: http://my.igpsport.com/webapi/WebLoginAuth ，并携带appid参数为iGPSPORT分配给第三方app的appid

### 获取用户信息以及Token

为WebView添加JavaScript接口用于接受返回的授权信息：

```java
class JsInvoker {

    @JavascriptInterface
	public void getLoginResult(String result) {

		//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("data", result);
		setResult(1000, intent);
		finish();
	}

}
```
设置name为app
```java
 wvAuth.addJavascriptInterface(new JsInvoker(), "app");

```
在getLoginResult方法中，result参数为json格式数据，包含用户ID，昵称，性别，身高体重和Token，Token过期时间等信息。

### 上传Fit文件
上传接口为：http://my.igpsport.com/Partner/UplodFit ，此接口要求下列四个参数：

```

* file ：文件

* memberid： 用户ID

* appid： APPID

* token：用户授权token
```

上传结果会返回int数值，正表表示改线路的id，此时以为上传成功，负值为错误代码。

```
 -10001 会员不能为空
 -10002 AppID不能为空
 -10003 Token不能为空
 -10004 AppId无效
 -10005 用户授权信息错误（不存在或者过期）
 -20001 file不能为空
 -20002 文件太大
 -20003 文件类型不支持
```
参考代码如下：
```java
File myFile = new File(filename);

RequestParams params = new RequestParams();
try {
    params.put("file", myFile);
} catch (Exception ex) {
	Toast.makeText(getApplicationContext(), "无法上传该文件", Toast.LENGTH_SHORT).show();
	return;
}
params.add("memberid", mUser.MemberID + "");
params.add("appid", Constants.APPID + "");
params.add("token", mUser.Token + "");

final ProgressDialog progressDialog = ProgressDialog.show(getApplicationContext(), "", "上传中...");
AsyncHttpClient myClient = new AsyncHttpClient();
myClient.post(getApplicationContext(), Constants.API_UPLOAD_FIT, params, new AsyncHttpResponseHandler() {
	@Override
	public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		progressDialog.dismiss();
		String result = new String(responseBody);
		Toast.makeText(getApplicationContext(), "上传成功，结果：" + result, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
		progressDialog.dismiss();
		Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
	}
});
```


