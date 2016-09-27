package com.bazlur.jobseek;

import org.springframework.stereotype.Component;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
@Component
public interface Searcher {
    boolean foundNew();

    boolean sendEmail();
}
