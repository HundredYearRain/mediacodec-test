package com.devkkh.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class PCMPlay {
	private static final String tag="pcm";
	int mSampleRate, mChannels;
	AudioTrack mAudioTrack;
	public PCMPlay(int sample_rate, int channels) {
		mSampleRate = sample_rate;
		mChannels = channels;
		
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, 
				AudioFormat.ENCODING_PCM_16BIT, 1920*3, AudioTrack.MODE_STREAM);
		
				
	}
	
	public int play() {
		mAudioTrack.setPlaybackRate(mSampleRate);
		mAudioTrack.play();
		return 0;
	}
	
	public void stop() {
		mAudioTrack.stop();
	}
	
	public void write(byte[] data, int size) {
		mAudioTrack.write(data, 0, size);
	}

}
