package com.denimexpertexpo.denimexpo.BackendHttp;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Handler;

/**
 * Created by ratul on 7/13/2015.
 */
public class AsyncHttpClient extends AsyncTask <String, String, String>{

    //some external constant to be used outside the class
    public static final String LOCATION_API_URL = "http://apps.bangladeshdenimexpo.com/api/location.php";
    public static final String SCHEDULE_API_URL = "http://apps.bangladeshdenimexpo.com/api/schedule.php";
    public static final String EVENTMAP_API_URL = "http://apps.bangladeshdenimexpo.com/api/eventmap.php";
    public static final String VISITOR_SUMMARY_API_URL = "http://visitor.bangladeshdenimexpo.com/index.php/test/TotalVisitor";

    private static final String VISITOR_API_BASE = "http://apps.bangladeshdenimexpo.com/api/visitors.php";
    public static final String BuildVisitorApiUrl(long offset, long limit)
    {
        return VISITOR_API_BASE + "?offset=" + offset + "&limit=" + limit;
    }

    private static final String EXHIBITOR_API_BASE = "http://apps.bangladeshdenimexpo.com/api/exhibitors.php";
    public static final String BuildExhibitorApiUrl(long offset, long limit)
    {
        return EXHIBITOR_API_BASE + "?offset=" + offset + "&limit=" + limit;
    }

    private static final String BARCODE_API_BASE = "http://apps.bangladeshdenimexpo.com/api/barcodeinfo.php";
    public static final String BuildBarcodeApiUrl(String gen_id){
        try{
            return BARCODE_API_BASE + "?generated_id="+ URLEncoder.encode(gen_id, "UTF-8");
        }catch (UnsupportedEncodingException ex)
        {
            return BARCODE_API_BASE + "?generated_id="+ gen_id;
        }
    }

    private static final String LOGIN_API_BASE = "http://exhibitor.bangladeshdenimexpo.com/index.php/test/LoginVerify";
    public static final String BuildLoginApiUrl(String username, String password) throws UnsupportedEncodingException
    {
        return LOGIN_API_BASE + "?username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
    }


    //these api are used @ login & splash screen to fetch the logged in user data
    public static final String BuildSpecificVisitorDetailsApiUrl(String id)
    {
        return VISITOR_API_BASE + "?id="+ id;
    }
    public static final String BuildSpecificExhibitorDetailsApiUrl(String id)
    {
        return EXHIBITOR_API_BASE + "?id=" + id;
    }

    private static final String FEEDBACK_API_BASE = "http://apps.bangladeshdenimexpo.com/api/feedback.php";
    public static final String BuildFeedbackApiUrl(String username, String email, String comment, String rating)
            throws UnsupportedEncodingException
    {

        return FEEDBACK_API_BASE + "?email="+URLEncoder.encode(email, "UTF-8")+
                "&comment="+URLEncoder.encode(comment, "UTF-8")
                +"&rating="+URLEncoder.encode(rating, "UTF-8")+
                "&name="+URLEncoder.encode(username, "UTF-8");
    }

    private static final String REGISTRATION_API_BASE = "http://apps.bangladeshdenimexpo.com/api/registration.php";
    public static final String BuildRegistrationApiUrl(String data)
    {
        try{
            return REGISTRATION_API_BASE + "?data=" + URLEncoder.encode(data, "UTF-8");
        }catch (UnsupportedEncodingException ex)
        {
            Log.e("unsopported encoding", ex.toString());
        }
        return null;
    }




    private AsyncHttpRequestHandler mDelegate;


    //some internal constants
    private final String OK_MSG     = "OK";
    private final String ERROR_MSG  = "ERROR";

    private final String TAG        = "AsyncHttpClient.TAG";


    /**
     * Constructor for the AsyncHttpClient
     *
     * @param obj
     */
    public AsyncHttpClient(AsyncHttpRequestHandler obj)
    {
        this.mDelegate = obj;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {

            return ERROR_MSG;

        } catch (IOException e) {
            return ERROR_MSG;
        }
        return OK_MSG + responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        //now time to call the handler's methods
        if(this.mDelegate != null)
        {
            if(result.startsWith(OK_MSG))
            {
                // got the exact result
                this.mDelegate.onResponseRecieved(result.substring(OK_MSG.length()));
            }
            else if(result.startsWith(ERROR_MSG))
            {
                //got the error result
                this.mDelegate.onHttpErrorOccured();
            }
        }
        else
        {
            Log.e(TAG, "this.mHandler is not assigned");
        }
    }
}
