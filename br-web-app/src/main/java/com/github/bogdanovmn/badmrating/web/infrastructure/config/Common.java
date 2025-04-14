package com.github.bogdanovmn.badmrating.web.infrastructure.config;

import com.github.bogdanovmn.badmrating.core.LocalStorage;
import com.github.bogdanovmn.badmrating.sources.rnbf.RussianNationalBadmintonFederation;
import com.github.bogdanovmn.badmrating.sources.rnbf.junior.RussianNationalBadmintonFederationJunior;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class Common {

    @Bean
    LocalStorage rbnfLocalStorage(@Value("${local-storage.path}") String storagePath) {
        return new LocalStorage(storagePath, new RussianNationalBadmintonFederation());
    }

    @Bean
    LocalStorage rbnfJuniorLocalStorage(@Value("${local-storage.path}") String storagePath) {
        return new LocalStorage(storagePath, new RussianNationalBadmintonFederationJunior());
    }
}
