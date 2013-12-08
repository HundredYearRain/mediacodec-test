package com.example.testmediacodec;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.devkkh.audio.PCMPlay;

public class MainActivity extends Activity implements OnClickListener {
	private static final String tag="decmain";
	boolean useMediaExt=true;
	SurfaceView svFrame;
	Surface mSurface;
	SurfaceHolder mHolder;
	MediaCodec mDec, mAudioDec;
	Button btnDec;
	PCMPlay mPcmPlay;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initAudio();
        
        //initdec();
    }

    void initAudio() {
    	mPcmPlay = new PCMPlay(48000, 2); 
    }
    
    
    private void initViews() {
		// TODO Auto-generated method stub
		svFrame = (SurfaceView)findViewById(R.id.svFrame);
		mHolder = svFrame.getHolder();
		mHolder.addCallback(new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				Log.d(tag, "surfcae destroyed");
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				Log.d(tag, "surface created");
				if(mSurface == null) {
					mSurface = holder.getSurface();
				}
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.d(tag, "surface changed");
			}
		});
		
		btnDec = (Button)findViewById(R.id.btnDec);
		btnDec.setOnClickListener(this);
	}


	private void initdec() {
		// TODO Auto-generated method stub
    	File file = new File("/sdcard/Download/cam4min.mp4");
		
    	FileInputStream is;
    	FileDescriptor fd=null;
    	try {
			is = new FileInputStream(file);
			fd = is.getFD();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	MediaFormat format=null, aformat=null, tmpformat=null;
    	MediaExtractor ext=null;
    	int trackcnt;
    	if(useMediaExt==true) {
	    	ext = new MediaExtractor();
	    	ext.setDataSource(fd);
    	}
    	
    	
    	int i, vtrack=-1, atrack=-1;
    	String vmime=null, amime=null;
    	trackcnt = ext.getTrackCount();
    	Log.d(tag, "track count = "+trackcnt);
    	for(i=0;i<trackcnt;i++) {
    		String mime;
    		tmpformat = ext.getTrackFormat(i);
    		mime = tmpformat.getString(MediaFormat.KEY_MIME);
    		Log.d(tag, "mime = " + mime);
    		
    		//
    		if(mime.toLowerCase().startsWith("video")) {
    			Log.d(tag, "video track found = " + mime);
    			//Log.d(tag, String.format("w=%d, h=%d, color=%d", format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT)));
    			//Log.d(tag, String.format("frame rate=%d", format.getInteger(MediaFormat.KEY_FRAME_RATE)));
    			vtrack = i;
    			format = tmpformat;
    			vmime = mime;
    			//break;
    		} else if(mime.startsWith("audio/mp4a-latm")){
    			Log.d(tag, "audio track found");
    			Log.d(tag, String.format("samplerate=%d, channel count=%d", tmpformat.getInteger(MediaFormat.KEY_SAMPLE_RATE), tmpformat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)));
    			atrack = i;
    			aformat = tmpformat;
    			amime = mime;
    		}
    		
    		
    	}
    	
    	
    	/*
    	ByteBuffer[] codecinbufs;
    	ByteBuffer[] codecoutbufs;
    	if(vtrack >= 0) {
    		mDec = MediaCodec.createDecoderByType(vmime);
    		if(useMediaExt==true) {
    			mDec.configure(format, mSurface, null, 0);
    		} else {
    			format = MediaFormat.createVideoFormat("video/avc", 1280, 720);
    			mDec.configure(format, mSurface, null, 0);
    		}	
    		mDec.start();
    		codecinbufs = mDec.getInputBuffers();
    		codecoutbufs = mDec.getOutputBuffers();

    	} else {
    		return;
    	}
    	*/
    	
    	
    	mPcmPlay.play();
    	
    	ByteBuffer[] ainbufs=null;
    	ByteBuffer[] aoutbufs=null;
    	
    	if(atrack >= 0) {
    		mAudioDec = MediaCodec.createDecoderByType("audio/mp4a-latm");
    		//aformat = MediaFormat.createAudioFormat("audio/mp4a-latm", 48000, 2);
    		//aformat = ext.getTrackFormat(atrack);
    		//aformat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
    		mAudioDec.configure(aformat, null, null, 0);
    		mAudioDec.start();
    		ainbufs = mAudioDec.getInputBuffers();
    		aoutbufs = mAudioDec.getOutputBuffers();
    	}
    	
    	
    	
    	
    	
    	int inidx, outidx;
    	boolean eos=false;
    	long pts=0;
    	long psize=0;
    	
    	
    	RawFrameFile rst = new RawFrameFile("/sdcard/Download/out.raw");
    	
    	FileInputStream rawins;
    	
    	byte[] buf = new byte[200*1024];    	  	
    	
    	int readsize=0;
    	
    	
    	ext.selectTrack(atrack);
    	while(true) {
    		inidx = mAudioDec.dequeueInputBuffer(5000);
    		Log.d(tag, "in buf idx="+inidx);
    		if(inidx >= 0 && eos == false) {
    		
	    		ByteBuffer ibb = ainbufs[inidx];
	    		if(useMediaExt==true) {
			    		readsize = ext.readSampleData(ibb, 0);
			    		//byte[] tdata = new byte[3000000];
			    		//ibb.get(tdata, 0, ibb.limit());
			    		//ibb.position(0);
			    		pts = 0;
			    		if(readsize<=0)
			    		{	
			    			eos = true;
			    			readsize = 0;
			    		} else {
			    			pts = ext.getSampleTime();	
			    		}
	    		} else {
	    			
	    			
	    		}
	    		mAudioDec.queueInputBuffer(inidx, 0, readsize, pts, eos ? MediaCodec.BUFFER_FLAG_END_OF_STREAM:0);
	    		Log.d(tag, "== read size="+readsize+", pts1="+pts);
	    		if(!eos)
	    			ext.advance();
    		}
    		//...
    		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    		outidx = mAudioDec.dequeueOutputBuffer(info,  5000);
    		if(outidx >= 0) {
    			ByteBuffer ob = aoutbufs[outidx];
    			Log.d(tag, String.format("outidx=%d, info size=%d, limit=%d", outidx, info.size, ob.limit()));
    			ob.rewind();
    			ob.get(buf, 0, info.size);
    			
    			mPcmPlay.write(buf, info.size);
    			mAudioDec.releaseOutputBuffer(outidx, false);
    		}
    		else if(outidx == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
    			Log.d(tag, "outbuffer changed......");
    			aoutbufs = mAudioDec.getOutputBuffers();
    		}
    		else if(outidx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
    			Log.d(tag, "format changed...."+ mAudioDec.getOutputFormat());
    			
    		}
    		else if(outidx == MediaCodec.INFO_TRY_AGAIN_LATER) {
    			Log.d(tag, "try again....");
    			//eos = true;
    		}
    		
    		if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
    			Log.d(tag, "decoding out end of stream....");
    			break;
    		}
    	}
    	
    	
    	/*
    	ext.selectTrack(vtrack);
    	while(!eos) {
    		inidx = mDec.dequeueInputBuffer(5000);
    		Log.d(tag, "in buf idx="+inidx);
    		if(inidx >= 0) {
    		
	    		ByteBuffer ibb = codecinbufs[inidx];
	    		if(useMediaExt==true) {
			    		readsize = ext.readSampleData(ibb, 0);
			    		byte[] tdata = new byte[3000000];
			    		//ibb.get(tdata, 0, ibb.limit());
			    		//ibb.position(0);
			    		pts = 0;
			    		if(readsize<=0)
			    		{	
			    			eos = true;
			    			readsize = 0;
			    		} else {
			    			pts = ext.getSampleTime();	
			    		}
	    		} else {
	    			
	    			Frame frame = new Frame(200*1024);
	    			readsize = rst.readFrame(frame);
	    			
	    			if(readsize>0) {
	    				int l = ibb.limit();
	    				int p = ibb.position();
	    				ibb.clear();
	    				ibb.put(frame.data, 0, frame.size);
	    				pts = frame.pts;
	    				//ibb.position(0);
	    				//ibb.flip();
	    				
	    			}	
	    			else
	    				eos = true;
	    		}
	    		mDec.queueInputBuffer(inidx, 0, readsize, pts, eos ? MediaCodec.BUFFER_FLAG_END_OF_STREAM:0);
	    		Log.d(tag, "== read size="+readsize+", pts1="+pts);
	    		if(!eos)
	    			ext.advance();
    		}
    		//...
    		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    		outidx = mDec.dequeueOutputBuffer(info,  5000);
    		if(outidx >= 0) {
    			ByteBuffer ob = codecoutbufs[outidx];
    			Log.d(tag, String.format("info size=%d, limit=%d", info.size, ob.limit()));
    			
    			
    			mDec.releaseOutputBuffer(outidx, true);
    		}
    		
    	}*/
    	
    	
    	//mDec.stop();
    	//mDec.release();
    	
    	mAudioDec.stop();
    	mAudioDec.release();
    	
    	Log.d(tag, "end.........");
    	
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0 == btnDec) {
			initdec();
		}
	}
    
}



class RawFrameFile {
	FileInputStream mStream;
	RawFrameFile(String fname) {
		try {
			mStream = new FileInputStream(fname);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	int read(byte[] buf, int size) {
		try {
			return mStream.read(buf, 0, size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	int readFrame(Frame frame) {
		byte[] intbuf = new byte[4];
		byte[] longbuf = new byte[8];
		
		int rdcnt=-1;
		
		try {
			ByteBuffer bb = ByteBuffer.wrap(intbuf);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			rdcnt = mStream.read(intbuf, 0, 4);
			
			if(rdcnt<4)
				return -1;
			//psize = (0x000000ff & intbuf[0]) | ((0x000000ff&intbuf[1]) << 8) | ((0x000000ff & intbuf[2]) << 16) | ((0x000000ff & intbuf[3]) << 24);
			frame.size = bb.getInt();

			
			
			bb = ByteBuffer.wrap(longbuf);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			rdcnt = mStream.read(longbuf, 0, 8);
			if(rdcnt<8)
				return -1;
			frame.pts = bb.getLong() *10;
			
			Log.d("rdframe", "frame rdcnt="+frame.size+", pts="+frame.pts);		
			
			
			rdcnt = mStream.read(frame.data, 0, frame.size);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rdcnt;
	}
}

class Frame {
	byte[] data;
	int size;
	long pts;
	Frame(int framesize) {
		data = new byte[framesize];
	}
}