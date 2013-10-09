package org.openhab.binding.xbmc.internal.client.messages;

public class XBMCMessage {

	private String jsonrpc = "2.0";
	private String method;
	private XBMCParams params;

	public String getJsonrpc() {
		return jsonrpc;
	}

	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public XBMCParams getParams() {
		return params;
	}

	public void setParams(XBMCParams params) {
		this.params = params;
	}

}
