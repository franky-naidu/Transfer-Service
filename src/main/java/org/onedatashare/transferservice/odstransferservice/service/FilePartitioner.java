package org.onedatashare.transferservice.odstransferservice.service;

import lombok.Getter;
import org.onedatashare.transferservice.odstransferservice.model.FilePart;
//import org.onedatashare.transferservice.odstransferservice.service.step.vfs.VfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.onedatashare.transferservice.odstransferservice.constant.ODSConstants.SIXTYFOUR_KB;
public class FilePartitioner {
    Logger logger = LoggerFactory.getLogger(FilePartitioner.class);
    ConcurrentLinkedQueue<FilePart> queue;
    public int chunkSize;

    public FilePartitioner(){
        this.queue = new ConcurrentLinkedQueue<>();
        this.chunkSize = SIXTYFOUR_KB;
    }

    public FilePartitioner(int chunkSize){
        this.queue = new ConcurrentLinkedQueue<>();
        this.chunkSize = chunkSize;
    }

    public FilePart nextPart(){
        return this.queue.poll();
    }

    /**
     * Returning -1 means an error occured.
     * @param totalSize
     * @param fileName
     * @return
     */
    public int createParts(long totalSize, String fileName){
        if(totalSize < 1) return -1;
        if(totalSize <= this.chunkSize){
            FilePart part = new FilePart();
            part.setFileName(fileName);
            part.setStart(0);
            part.setEnd(totalSize);
            part.setSize(Long.valueOf(totalSize).intValue());
            queue.add(part);
        }else{
            long startPosition = 0;
            long chunksOfChunksKB = totalSize / this.chunkSize;
            for(long i = 0; i < chunksOfChunksKB; i++){
                FilePart part = new FilePart();
                part.setFileName(fileName);
                part.setPartIdx(i);
                part.setSize(this.chunkSize);
                part.setStart(startPosition);
                startPosition+=this.chunkSize;
                part.setEnd(startPosition);
                this.queue.add(part);
            }
            FilePart lastChunk = new FilePart();
            lastChunk.setStart(startPosition);
            lastChunk.setFileName(fileName);
            lastChunk.setSize(Long.valueOf(totalSize-startPosition).intValue());
            lastChunk.setPartIdx(chunksOfChunksKB);
            lastChunk.setEnd(totalSize);
            queue.add(lastChunk);
        }
        logger.info("The total size of the queue after parsing file: " + fileName +" " +queue.size());
        return queue.size();
    }

}
