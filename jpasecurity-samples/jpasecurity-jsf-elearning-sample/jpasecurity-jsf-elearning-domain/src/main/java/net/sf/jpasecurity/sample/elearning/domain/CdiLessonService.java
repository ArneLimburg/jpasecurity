/*
 * Copyright 2011 Raffaela Ferrari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.sample.elearning.domain;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import net.sf.jpasecurity.sample.elearning.core.Current;
import net.sf.jpasecurity.sample.elearning.core.Transactional;

/**
 * @author Raffaela Ferrari
 */
@Named("lessonService")
public class CdiLessonService implements LessonService {

    @Inject
    private Provider<Lesson> lessonProvider;
    @Inject
    private Provider<Course> courseProvider;
    @Inject @Current
    private Provider<Student> studentProvider;

    @Transactional
    public void finish() {
        courseProvider.get().finishLesson(studentProvider.get(), lessonProvider.get());
    }

    public boolean isFinished() {
        return courseProvider.get() != null? courseProvider.get()
            .isLessonFinished(studentProvider.get(), lessonProvider.get()) : false;
    }

    public boolean isNotFinished() {
        return courseProvider.get() != null? !courseProvider.get()
            .isLessonFinished(studentProvider.get(), lessonProvider.get()) : true;
    }

    public boolean isNotStarted() {
        return lessonProvider.get() != null? !lessonProvider.get()
            .equals((courseProvider).get().getCurrentLession(studentProvider.get())) : true;
    }

    public boolean isStarted() {
        return lessonProvider.get() != null? lessonProvider.get()
            .equals((courseProvider).get().getCurrentLession(studentProvider.get())) : false;
    }

    @Transactional
    public void start() {
        courseProvider.get().startLesson(studentProvider.get(), lessonProvider.get());
    }
}
