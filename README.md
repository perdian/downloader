# Introduction

The downloader application implements a simple but powerful and extensible
Java downloader engine that can either be used embedded in another application
(by using the DownloadEngine directly) or as standalone client.

# Usage

## Flow

We want to decouple the actual execution of a download as best as possible from the information needed to perform that execution.

It may be the case that in order to find the resource (or resources) that should be downloaded a lengthy discovery process needs to be performed.
However that discovery process should only happen right before the actual download is being performed to avoid any kind of timeout between computing the resource that should be downloaded and the download itself.

So from a high level point of view the steps to get to the actual download are as follows

* The client creates a `DownloadRequest` in which he defined *what* data should be downloaded and how to compute the actual resources from which to retrieve the data.
* If the `DownloadEngine` accepts a `DownloadRequest` and stores it in the internal queue until a free download slot can be alloted.
* When a free download slot can be alloted the `DownloadRequest` will be transformed into a `DownloadTask` which will then be used to actually transfer the bytes from the remote resource to the local file system.
* While the data transfer is in place a `DownloadOperation` will be provided inside the `DownloadEngine` to monitor the progress.

# Thanks to

* Material design icons (https://material.io/tools/icons/)
