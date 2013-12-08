package com.devkkh.rmedia;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

public class RAudio {
	private static final String tag="ra";
	MediaCodec mDec;
	MediaFormat mFormat;
	ByteBuffer[] inbufs;
	ByteBuffer[] outbufs;
	MediaExtractor ext;
	
	//
	RAudio() {
		mFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 48000, 2);
		mDec = MediaCodec.createDecoderByType("audio/mp4a-latm");
		mDec.configure(mFormat, null, null, 0);
		
	}
	
	//
	int play() {
		mDec.start();
		inbufs = mDec.getInputBuffers();
		outbufs = mDec.getOutputBuffers();
		
		//ext = MediaExtractor.set
		return -1;
	}
}
