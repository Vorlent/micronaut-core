/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.context.router;

import org.junit.Test;
import org.particleframework.context.BeanContext;
import org.particleframework.context.DefaultBeanContext;
import org.particleframework.http.HttpMethod;

import javax.inject.Inject;

import java.util.List;

import static org.particleframework.http.MediaType.*;
import static org.junit.Assert.*;
/**
 * @author Graeme Rocher
 * @since 1.0
 */
public class RouteBuilderTests {

    @Test
    public void testRouterBuilder() {
        MyRouteBuilder routeBuilder = new MyRouteBuilder(new DefaultBeanContext().start());
        routeBuilder.someRoutes(new BookController(), new AuthorController());
        List<Route> builtRoutes = routeBuilder.getBuiltRoutes();

        assertTrue(builtRoutes
                        .stream()
                        .anyMatch(route ->
                                route.match("/books/1").isPresent() && route.getHttpMethod() == HttpMethod.GET)
                        );
        assertTrue(builtRoutes
                .stream()
                .anyMatch(route ->
                        route.match("/books/1/authors").isPresent() && route.getHttpMethod() == HttpMethod.GET)
        );
        assertTrue(builtRoutes
                .stream()
                .anyMatch(route ->
                        route.match("/books").isPresent() && route.getHttpMethod() == HttpMethod.POST)
        );
        assertTrue(builtRoutes
                .stream()
                .anyMatch(route ->
                        route.match("/book").isPresent() && route.getHttpMethod() == HttpMethod.POST)
        );
        assertFalse(builtRoutes
                .stream()
                .anyMatch(route ->
                        route.match("/boo").isPresent() && route.getHttpMethod() == HttpMethod.POST)
        );
        assertTrue(builtRoutes
                .stream()
                .anyMatch(route ->
                        route.match("/book/1").isPresent() && route.getHttpMethod() == HttpMethod.GET)
        );
    }

    static class MyRouteBuilder extends DefaultRouteBuilder {
        public MyRouteBuilder(BeanContext beanContext) {
            super(beanContext);
        }

        @Inject
        void someRoutes(BookController controller, AuthorController authorController) {
            GET("{/message}", controller, "hello").accept(JSON);
            GET("/books{/id}", controller, "show").nest(() ->
                    GET("/authors", controller)
            );
            GET(controller);
            POST(controller);
            PUT(controller, ID);
            GET(controller, ID);
            GET(BookController.class, ID);
            GET(Book.class, ID);
            GET(controller, ID).nest(() ->
                    GET(authorController)
            );

            GET("/books", controller);
            POST("/books", controller, "save").accept(JSON);

            // handle errors
            error(RuntimeException.class, controller, "error");
            // handle status codes
            status(404, controller, "notFound");

            // REST resources
            resources(controller);
            resources(Book.class);
//            single(Book.class).nest(()->
//                resources(Author.class)
//            );
        }
    }

    static class BookController {
        String hello(String message) {
            return "Hello " + message;
        }
    }

    static class AuthorController {
        String hello(String message) {
            return "Hello " + message;
        }
    }

    static class Author {}
    static class Book {}
}
