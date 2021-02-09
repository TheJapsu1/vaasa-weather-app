package com.japsu.vaasaweather;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Switch;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class TaskParams
{
    URL url;
    boolean isChecked;

    TaskParams(URL url, boolean isChecked)
    {
        this.url = url;
        this.isChecked = isChecked;
    }
}

public class Warnings
{
    public static void GetWarnings(boolean switchState)
    {
        try
        {
            TaskParams pars = new TaskParams(new URL("https://alerts.fmi.fi/cap/feed/rss_fi-FI.rss"), switchState);
            new DownloadWarnings().execute(pars);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}

class DownloadWarnings extends AsyncTask<TaskParams, Integer, List<String>>
{
    @Override
    protected List<String> doInBackground(TaskParams... params)
    {
        URL urls = params[0].url;
        boolean isChecked = params[0].isChecked;

        List<String> result = new ArrayList<String>();
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(urls.toString()).openStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if(doc == null)
        {
            return null;
        }

        NodeList results = doc.getElementsByTagName("item");
        NodeList description = doc.getElementsByTagName("description");

        //add the main title to result
        result.add("<big>" + description.item(0).getTextContent() + ":</big><br><br>");

        for(int i = 0; i < results.getLength(); i++)
        {
            NodeList contents = results.item(i).getChildNodes();

            for(int j = 0; j < contents.getLength(); j++)
            {
                if(isChecked)
                {
                    Log.d("TESTING", "Getting info with CHECKED");
                    if(contents.item(j).getNodeName().contains("title") && contents.item(j).getTextContent().toLowerCase().contains(" ".toLowerCase()))
                    {
                        //get the title of the warning, and add a linebreak inside it and capsulate the time and date
                        String title = contents.item(j).getTextContent();
                        StringBuilder sb = new StringBuilder(title);
                        sb.insert(title.indexOf(',') + 2, "<br>(");
                        sb.append(")");
                        //add the title to result
                        result.add("<br><b>" + sb.toString() + "</b>");

                        for(int k = 0; k < contents.getLength(); k++)
                        {
                            if(contents.item(k).getNodeName().contains("description"))
                            {
                                //add the additional info of the warning to result
                                result.add("<br>   -Lisätietoa: <dfn>" + contents.item(k).getTextContent() + "</dfn><br>");
                            }
                        }
                    }
                }
                else
                {
                    Log.d("TESTING", "Getting info with NOT CHECKED");
                    if(contents.item(j).getNodeName().contains("title") && (contents.item(j).getTextContent().toLowerCase().contains("koko maa".toLowerCase()) || contents.item(j).getTextContent().toLowerCase().contains("Vaasa".toLowerCase()) || contents.item(j).getTextContent().toLowerCase().contains("Pohjanmaa".toLowerCase())))
                    {
                        //get the title of the warning, and add a linebreak inside it and capsulate the time and date
                        String title = contents.item(j).getTextContent();
                        StringBuilder sb = new StringBuilder(title);
                        sb.insert(title.indexOf(',') + 2, "<br>(");
                        sb.append(")");
                        //add the title to result
                        result.add("<br><b>" + sb.toString() + "</b>");

                        for(int k = 0; k < contents.getLength(); k++)
                        {
                            if(contents.item(k).getNodeName().contains("description"))
                            {
                                //add the additional info of the warning to result
                                result.add("<br>   -Lisätietoa: <dfn>" + contents.item(k).getTextContent() + "</dfn><br>");
                            }
                        }
                    }
                }
            }

            if(i > 0)
            {
                publishProgress(100 / (results.getLength() / i));
            }
            else
            {
                publishProgress(0);
            }
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        Fragment1.UpdateTextfield("Ladataan XML dataa...: " + progress[0] + "%");
    }

    @Override
    protected void onPostExecute(List<String> result)
    {
        if(result != null)
        {
            Fragment1.UpdateTextfield(result);
        }
        else
        {
            Fragment1.UpdateTextfield("Ei internetyhteyttä");
        }
    }
}