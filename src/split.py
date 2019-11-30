#!/usr/bin/env python
# -*- coding:utf-8 -*-
import os
import wave
import struct
import math
from scipy import fromstring, int16

## waveファイルを切り取る
def cut_wav(filename, time):
    # timeの単位は[sec]

    # ファイルを読み出し
    wr = wave.open(filename, 'r')

    # waveファイルが持つ性質を取得
    ch = wr.getnchannels()
    width = wr.getsampwidth()
    fr = wr.getframerate()
    fn = wr.getnframes()
    total_time = 1.0 * fn / fr
    integer = math.floor(total_time) # 小数点以下切り捨て
    t = int(time)  # 秒数[sec]
    frames = int(ch * fr * t)
    num_cut = int(integer//t)

    # 確認用
    print("Channel: ", ch)
    print("Sample width: ", width)
    print("Frame Rate: ", fr)
    print("Frame num: ", fn)
    print("Params: ", wr.getparams())
    print("Total time: ", total_time)
    print("Total time(integer)",integer)
    print("Time: ", t)
    print("Frames: ", frames)
    print("Number of cut: ",num_cut)

    # waveの実データを取得し、数値化
    data = wr.readframes(wr.getnframes())
    wr.close()
    X = fromstring(data, dtype=int16)
    print(X)


    for i in range(num_cut):
        print(i)
        # 出力データを生成
        outf = 'output/' + str(i) + '.wav'
        start_cut = i*frames
        end_cut = i*frames + frames
        print(start_cut)
        print(end_cut)
        Y = X[start_cut:end_cut]
        outd = struct.pack("h" * len(Y), *Y)

        # 書き出し
        ww = wave.open(outf, 'w')
        ww.setnchannels(ch)
        ww.setsampwidth(width)
        ww.setframerate(fr)
        ww.writeframes(outd)
        ww.close()

args = os.sys.argv

if len(args) < 3:
    print("[usage] {0} <wave file> <time in sec>".format(args[0]))
else:
    # 一応既に同じ名前のディレクトリがないか確認。
    file = os.path.exists("output")

    if file == False:
        #保存先のディレクトリの作成
        os.mkdir("output")

    f_name = args[1]
    cut_time = int(args[2])

    print("Devide ${0} into the wave files for ${1}".format(f_name, cut_time))
    cut_wav(f_name,cut_time)
