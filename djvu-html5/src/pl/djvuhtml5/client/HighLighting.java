package pl.djvuhtml5.client;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import java.util.ArrayList;


public class HighLighting
{
    private ArrayList<HighLight> highLights = null;


    public HighLighting(String highlights_url)
    {
        this.setup(highlights_url);
    }


    public void draw(int page, Context2d graphics2d, int startX, int startY, double zoom, int page_height)
    {
        for(int i=0; i<this.highLights.size(); i++)
        {
            HighLight highLight = this.highLights.get(i);

            if( (page+1) == highLight.page)
            {
                graphics2d.setFillStyle( highLight.color );
                graphics2d.fillRect((highLight.x_start * zoom) + startX , ((page_height - highLight.y_start) * zoom) + startY , highLight.width * zoom, -highLight.height * zoom);
            }
        }
    }


    private void setup(String highlights_json)
    {
        this.highLights = new ArrayList<HighLight>();

        JSONObject jsonObject = (JSONObject) JSONParser.parseStrict(highlights_json);

        for(String k : jsonObject.keySet())
        {
            int page = Integer.parseInt(k);
            JSONArray page_highlights = (JSONArray) jsonObject.get(k);

            for(int i=0; i<page_highlights.size(); i++)
            {
                JSONObject h = (JSONObject) page_highlights.get(i);

                double x_start = h.get("x").isNumber().doubleValue();
                double y_start = h.get("y").isNumber().doubleValue();
                double width = h.get("width").isNumber().doubleValue();
                double height = h.get("height").isNumber().doubleValue();
                String color = h.get("color").isString().stringValue();

                highLights.add( new HighLight(page, x_start, y_start, width, height, color) );
            }
        }
    }


    private class HighLight
    {
        public int page;
        public double x_start;
        public double y_start;
        public double width;
        public double height;
        public String color;


        public HighLight(int page, double x_start, double y_start, double width, double height, String color)
        {
            this.page = page;
            this.x_start = x_start;
            this.y_start = y_start;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

}



