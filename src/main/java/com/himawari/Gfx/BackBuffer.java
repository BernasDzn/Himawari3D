package com.himawari.Gfx;

import com.himawari.HLA.Vec3;
import com.himawari.Utils.Utils;
import com.himawari.Utils.Window;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BackBuffer {

    // Buffer data
    public static int bufferWidth, bufferHeight;
    public static ByteBuffer colorBuffer;

    // initialize the buffer with the respective window dimensions
    public static void Init(){

        BackBuffer.bufferWidth = Window.getInstance().config().width;
        BackBuffer.bufferHeight = Window.getInstance().config().height;

        BackBuffer.colorBuffer = ByteBuffer.allocateDirect(bufferWidth * bufferHeight * 4).order(ByteOrder.nativeOrder());

        ClearBackBuffer();
    }

    public static void PutPixel(int x, int y, Color color, Vec3 v1, Vec3 v2, Vec3 v3){

        if(x < 0 || y < 0 || x >= bufferWidth || y >= bufferHeight) return;

        int position = x * 4 + y * bufferWidth * 4;

        // When no surface is given skip Depth buffering
        if(v1 == null || v2 == null || v3 == null){

            colorBuffer.position(position);
            byte[] colorBytes = color.toByte();
            colorBuffer.put(colorBytes);

        } else{

            // Calculate the pixel projection onto surface
            float depth = ZBuffer.ProjectOntoToFace(v1, v2, v3, new Vec3(x, y, 0)).z;
            boolean result = ZBuffer.TestAndSet(x, y, depth);

            if(result) {

                colorBuffer.position(position);
                byte[] colorBytes = color.toByte();
                colorBuffer.put(colorBytes);
            }
        }
    }

    public static void ClearBackBuffer(){
        
        colorBuffer.clear(); // Reset position to 0
        byte[] black = Color.BLACK.toByte();
        
        for(int i = 0; i < bufferWidth * bufferHeight; i++) {
            colorBuffer.put(black);
        }
        
        colorBuffer.rewind(); // Reset position to 0 after filling
    }
    
    public static void FillBufferLine(float x1, float y1, float x2, float y2, Color fillValue){

        float dx = x2 - x1;
        float dy = y2 - y1;

        int step = (int) (dx > dy ? dx : dy);

        if (step == 0) {
            PutPixel((int) x1, (int) y1, fillValue, null, null, null);
            return;
        }

        float xIncrement = dx / step;
        float yIncrement = dy / step;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= step; i++) {
            if (y >= bufferHeight || x >= bufferWidth || x < 0 || y < 0) break;
            
            PutPixel((int) x, (int) y, fillValue, null, null, null);

            x += xIncrement;
            y += yIncrement;
        }
    }

    // Simple algorithm for filling triangles
    public static void FillBufferTriangle(Vec3 v1, Vec3 v2, Vec3 v3, Color color) {
       
        int maxX = (int) Math.max(v1.x, Math.max(v2.x, v3.x));
        int minX = (int) Math.min(v1.x, Math.min(v2.x, v3.x));
        int maxY = (int) Math.max(v1.y, Math.max(v2.y, v3.y));
        int minY = (int) Math.min(v1.y, Math.min(v2.y, v3.y));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                
                if (PointInTriangle(new Vec3(x,y,0), v1, v2, v3)) {
                    PutPixel(x, y, color, v1, v2, v3);
                }
            }
        }
    }

    // Check if point inside triangle
    private static boolean PointInTriangle (Vec3 pt, Vec3 v1, Vec3 v2, Vec3 v3) {
        
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(pt, v1, v2);
        d2 = sign(pt, v2, v3);
        d3 = sign(pt, v3, v1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    private static float sign (Vec3 p1, Vec3 p2, Vec3 p3) {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    // Turn current backbuffer into SDL_Texture
    // public static SDL_Texture FetchTexture(SDL_Renderer renderer) {

    //     // Allocate the buffer of correct size
    //     int bufferSize = bufferWidth * bufferHeight * 4;
    //     Pointer pointer = new Memory(bufferSize);
    //     pointer.write(0, FlattenBuffer(), 0, bufferSize);

    //     SDL_Texture output = SDL_CreateTexture(renderer, SDL_PixelFormatEnum.SDL_PIXELFORMAT_RGBA32, SDL_TextureAccess.SDL_TEXTUREACCESS_STREAMING, bufferWidth, bufferHeight);
    //     SDL_UpdateTexture(output, null, pointer, bufferWidth * 4);

    //     pointer.clear(bufferSize);
    //     pointer = null;

    //     return output;
    // }

    public static byte[] FlattenBuffer(){

        byte[] flatBuffer = new byte[bufferWidth * bufferHeight * 4];

        if (Renderer.renderTarget == RenderTarget.COLORBUFFER) {        
    
            for (int i = 0; i < flatBuffer.length; i+=4) {
    
                int index = i / 4;
                byte[] colorBytes = new byte[4];
    
                colorBuffer.position(index * 4);
                colorBuffer.get(colorBytes);
    
                Color c = new Color(colorBytes);
    
                flatBuffer[i] = c.r;
                flatBuffer[i + 1] = c.g;
                flatBuffer[i + 2] = c.b;
                flatBuffer[i + 3] = c.a;
            }
        } else if (Renderer.renderTarget == RenderTarget.ZBUFFER) {
    
            for (int i = 0; i < flatBuffer.length; i+=4) {
    
                int index = i / 4;
                //byte[] colorBytes = new byte[4];
                float value = ZBuffer.depthBuffer.get(index);
    
                Color c = new Color((int)value, (int)value, (int)value, 255);
    
                flatBuffer[i] = c.r;
                flatBuffer[i + 1] = c.g;
                flatBuffer[i + 2] = c.b;
                flatBuffer[i + 3] = c.a;
            }
    
            return flatBuffer;
        }

        return flatBuffer;
    }
}
