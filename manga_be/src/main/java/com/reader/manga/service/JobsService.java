package com.reader.manga.service;

import com.reader.manga.enums.JobsType;
import com.reader.manga.job.manga.ColetorManga;
import com.reader.manga.vo.job.manga.MangaJobVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobsService {

    private final ColetorManga coletorManga;

    public MangaJobVO executaJobManga(String manga) {
        Mono<MangaJobVO> executa = coletorManga.executa(manga);
        return executa.block();
    }

    public List<JobsType> getJobs() {
        return Arrays.asList(JobsType.values());
    }
}
