package me.david.webapi.response.error;

import me.david.davidlib.util.ExceptionUtil;
import me.david.webapi.response.Response;
import me.david.webapi.response.content.string.StaticStringContent;

public class DefaultErrorFactory implements ErrorFactory {

    @Override
    public Response generatePage(Throwable throwable) {
        Response response = new Response(new StaticStringContent("<h1>Internal Server Error</h1> <code><pre>" + ExceptionUtil.toString(throwable) + "</pre></code>"));
        response.setResponseCode(500);
        return response;
    }

    @Override
    public boolean valid(Throwable throwable) {
        return true;
    }

}
