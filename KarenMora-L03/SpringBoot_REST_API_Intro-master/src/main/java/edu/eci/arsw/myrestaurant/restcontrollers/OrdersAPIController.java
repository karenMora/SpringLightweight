/*
 * Copyright (C) 2016 Pivotal Software, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.eci.arsw.myrestaurant.restcontrollers;

import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.model.ProductType;
import edu.eci.arsw.myrestaurant.model.RestaurantProduct;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServicesStub;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hcadavid
 */

@RestController
@RequestMapping(value="/orders")
public class OrdersAPIController {
    
    //@Service-> la clase que se inyecta
    //@Autowired-> Para lo que se le va a hacer inyection
    @Autowired
    RestaurantOrderServices restaurantOrderServices;
    
    Set<Integer> pedidos;
    Order orden;
    
    public OrdersAPIController(RestaurantOrderServices restOrdServ){
        restaurantOrderServices=restOrdServ;
    }
    
    /**
     * Haga que en esta misma clase se inyecte el bean de tipo 
     * RestaurantOrderServices, y que a éste -a su vez-, se le inyecte el bean 
     * BasicBillCalculator
     * 
     * @return 
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> manejadorGetRecursoOrders(){
        Set<Order> ordenes=new HashSet<>();
        try{
            pedidos=restaurantOrderServices.getTablesWithOrders();
            for(Integer tabla:pedidos){
                orden=restaurantOrderServices.getTableOrder(tabla);
                ordenes.add(orden);
            }
            //obtener datos que se enviarán a través del API
            return new ResponseEntity<>(ordenes, HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Error bla bla bla", HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Modifique el controlador para que ahora, adicionalmente, acepte 
     * peticiones GET al recurso /orden/{idmesa}, donde {idmesa} es el número 
     * de una mesa en particular.
     * 
     * @param idmesa
     * @return 
     */
    @RequestMapping(path="/{idmesa}",method=RequestMethod.GET)
    public ResponseEntity<?>manejadorGetRecursoOrders(@PathVariable int idmesa){
        Set<Order> ordenes=new HashSet<>();
        try{
            pedidos=restaurantOrderServices.getTablesWithOrders();
                orden=restaurantOrderServices.getTableOrder(idmesa);
                ordenes.add(orden);
            //obtener datos que se enviarán a través del API
            return new ResponseEntity<>(ordenes, HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Error bla bla bla", HttpStatus.NOT_FOUND);
        }
    }
    
    
    /**
     * Agregue el manejo de peticiones POST (creación de nuevas ordenes), de 
     * manera que un cliente http pueda registrar una nueva orden haciendo una 
     * petición POST al recurso ‘ordenes’, y enviando como contenido de la 
     * petición todo el detalle de dicho recurso a través de un documento jSON.
     */
    @RequestMapping(method= RequestMethod.POST)
    public ResponseEntity<?> manejadorPotRecursoOrders(@RequestBody Order o) {
        try {
            restaurantOrderServices.addNewOrderToTable(o);
            //registrar dato
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Error bla bla bla", HttpStatus.FORBIDDEN);
        }
    }
    
    
    /**
     * Haga lo necesario para que ahora el API acepte peticiones al recurso
     * '/orders/{idmesa}/total, las cuales retornen el total de la cuenta de 
     * la orden {idorden}
     * 
     */
    @RequestMapping(path="/{idmesa}/total",method=RequestMethod.GET)
    public ResponseEntity<?> manejadorGtRecursoOrders(@RequestBody int idorden){
        try{
            //obtener datos que se enviarán a través del API
            return new ResponseEntity<>(restaurantOrderServices.calculateTableBill(idorden), HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Error bla bla bla", HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Se requiere que el API permita agregar un producto a una orden. Revise
     * acá cómo se debe manejar el verbo PUT con este fin, y haga la 
     * implementación en el proyecto.
     * 
     */
    @RequestMapping(path="/{idorden}",method=RequestMethod.PUT)
    public ResponseEntity<?> manejadorPutRecursoOrders(@PathVariable int idorden, @RequestBody Order o){
        try{
            restaurantOrderServices.releaseTable(idorden);
            restaurantOrderServices.addNewOrderToTable(o);
            //obtener datos que se enviarán a través del API
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Error bla bla bla", HttpStatus.NOT_FOUND);
        }
    }
    
    
    /**
     * Se requiere que el API permita cancelar la orden de una mesa. Agregue
     * esta funcionalidad teniendo en cuenta que de acuerdo con el estilo REST,
     * ésto se debería poder hacer usando el verbo DELETE en el 
     * recurso /orders/{idmesa}.
     * 
     */
    @RequestMapping(path="/{idorden}",method=RequestMethod.DELETE)
    public ResponseEntity<?> manejadorDeleteRecursoOrders(@PathVariable int idorden){
        try{
            restaurantOrderServices.releaseTable(idorden);
            //obtener datos que se enviarán a través del API
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Error bla bla bla", HttpStatus.NOT_FOUND);
        }
    }
    
}



































