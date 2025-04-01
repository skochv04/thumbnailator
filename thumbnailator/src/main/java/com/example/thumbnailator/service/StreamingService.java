package com.example.thumbnailator.service;

import com.example.thumbnailator.model.Size;
import com.example.thumbnailator.response.ThumbnailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class StreamingService {
    private Sinks.Many<ThumbnailResponse> sink;
    private final Logger logger = LoggerFactory.getLogger(StreamingService.class);

    public Flux<ThumbnailResponse> getOrCreateStream(Size size) {
        Sinks.Many<ThumbnailResponse> sink = getOrInitSink();

        return sink.asFlux()
                .filter(thumbnailResponse -> thumbnailResponse.size() == size);
    }

    public void emitThumbnail(ThumbnailResponse thumbnailResponse) {
        Sinks.Many<ThumbnailResponse> sink = getOrInitSink();
        Sinks.EmitResult result = sink.tryEmitNext(thumbnailResponse);
        if (result.isFailure()) {
            logger.error("Failed to emit thumbnails.");
        }
    }

    public boolean isStreamInitialized() {
        return sink != null;
    }

    public Sinks.Many<ThumbnailResponse> getOrInitSink() {
        if (!isStreamInitialized()){
            sink = Sinks.many().replay().all();
        }
        return sink;
    }
}
