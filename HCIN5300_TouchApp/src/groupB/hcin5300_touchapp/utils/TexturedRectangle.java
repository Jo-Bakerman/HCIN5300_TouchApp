package groupB.hcin5300_touchapp.utils;

import java.nio.Buffer;

public class TexturedRectangle extends MeshObject {
	
	private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;
    
    private int indicesNumber = 0;
    private int verticesNumber = 0;
	
	public TexturedRectangle()
    {
        setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }
	
	private void setVerts()
    {
        double[] PLANE_VERTS = 
        	{ -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};
        mVertBuff = fillBuffer(PLANE_VERTS);
        verticesNumber = PLANE_VERTS.length / 3;
    }
    
    
    private void setTexCoords()
    {
        double[] PLANE_TEX_COORDS = 
        	{ 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        mTexCoordBuff = fillBuffer(PLANE_TEX_COORDS);
        
    }
    
    
    private void setNorms()
    {
        double[] PLANE_NORMS = 
        	{ 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f};
        mNormBuff = fillBuffer(PLANE_NORMS);
    }
    
    
    private void setIndices()
    {
        short[] PLANE_INDICES = 
        	{0, 1, 2, 0, 2, 3};
        mIndBuff = fillBuffer(PLANE_INDICES);
        indicesNumber = PLANE_INDICES.length;
    }

    public int getNumObjectIndex()
    {
        return indicesNumber;
    }
    
    
    @Override
    public int getNumObjectVertex()
    {
        return verticesNumber;
    }
    
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
            default:
                break;
        
        }
        
        return result;
    }
}
