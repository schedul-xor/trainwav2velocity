#!/usr/bin/env python3
# -*- coding:utf-8 -*-
import glob
import wave
import struct
from scipy import fromstring, int16
import numpy as np
from pylab import *

def fourier(x, n, w):
    K = []
    for i in range(0, w-2):
        sample = x[i * n:( i + 1) * n]
        partial = np.fft.fft(sample)
        K.append(partial)

    return K

def do_fft(wavfile):
    wr = wave.open(wavfile, "rb")
    ch = wr.getnchannels()
    width = wr.getsampwidth()
    fr = wr.getframerate()
    fn = wr.getnframes()

    N = 256
    span = 15000

    print('チャンネル', ch)
    print('総フレーム数', fn)
    print('サンプル時間', 1.0 * N * span / fr, '秒')

    origin = wr.readframes(wr.getnframes())
    data = origin[:N * span * ch * width]
    wr.close()

    print('現配列長', len(origin))
    print('サンプル配列長: ', len(data))

    # ステレオ前提
    X = np.frombuffer(data, dtype="int16")
    left = X[::2]
    right = X[1::2]

    fft_ret = fourier(right, N, span)

    print(fft_ret)

args = sys.argv

if len(args) < 2:
    print("[usage] {0} <path of the input wave files>\n example) {0} 'output/*.wav'".format(args[0]))
else:
    waves_path = args[1]
    for p in glob.glob(waves_path):
        do_fft(p)
