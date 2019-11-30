#!/usr/bin/env python3
# -*- coding:utf-8 -*-
import wave
import glob
from pylab import *

def fft(wavfile):
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

args = sys.argv

if len(args) < 2:
    print("[usage] {0} <path of the input wave files>".format(args[0]))
else:
    waves_path = args[1]
    for p in glob.glob(waves_path):
        print(p)
