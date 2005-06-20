/**
 * AuthenticationSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package no.feide.moria.webservices.v2_2;

public class AuthenticationSoapBindingStub extends org.apache.axis.client.Stub implements no.feide.moria.webservices.v2_2.Authentication_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getUserAttributes");
        oper.addParameter(new javax.xml.namespace.QName("", "serviceTicket"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOfAttribute"));
        oper.setReturnClass(no.feide.moria.webservices.v2_2.Attribute[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getUserAttributesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.InternalException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.IllegalInputException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.UnknownTicketException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "UnknownTicketException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthorizationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("initiateAuthentication");
        oper.addParameter(new javax.xml.namespace.QName("", "attributes"), new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOf_soapenc_string"), java.lang.String[].class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "returnURLPrefix"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "returnURLPostfix"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "forceInteractiveAuthentication"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "initiateAuthenticationReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.InternalException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.IllegalInputException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthorizationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("directNonInteractiveAuthentication");
        oper.addParameter(new javax.xml.namespace.QName("", "attributes"), new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOf_soapenc_string"), java.lang.String[].class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "username"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "password"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOfAttribute"));
        oper.setReturnClass(no.feide.moria.webservices.v2_2.Attribute[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "directNonInteractiveAuthenticationReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.InternalException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.IllegalInputException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthenticationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthenticationFailedException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthorizationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthenticationUnavailableException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthenticationUnavailableException"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("proxyAuthentication");
        oper.addParameter(new javax.xml.namespace.QName("", "attributes"), new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOf_soapenc_string"), java.lang.String[].class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "proxyTicket"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOfAttribute"));
        oper.setReturnClass(no.feide.moria.webservices.v2_2.Attribute[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "proxyAuthenticationReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.InternalException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.IllegalInputException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.UnknownTicketException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "UnknownTicketException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthorizationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProxyTicket");
        oper.addParameter(new javax.xml.namespace.QName("", "ticketGrantingTicket"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "proxyServicePrincipal"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProxyTicketReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.InternalException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.IllegalInputException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.UnknownTicketException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "UnknownTicketException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthorizationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("verifyUserExistence");
        oper.addParameter(new javax.xml.namespace.QName("", "username"), new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "verifyUserExistenceReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.InternalException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.IllegalInputException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthorizationFailedException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "fault"),
                      "no.feide.moria.servlet.soap.AuthenticationUnavailableException",
                      new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthenticationUnavailableException"), 
                      true
                     ));
        _operations[5] = oper;

    }

    public AuthenticationSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public AuthenticationSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public AuthenticationSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOfAttribute");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.webservices.v2_2.Attribute[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "ServerException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.ServerException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthenticationUnavailableException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.AuthenticationUnavailableException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "ArrayOf_soapenc_string");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "IllegalInputException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.IllegalInputException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "InternalException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.InternalException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthenticationFailedException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.AuthenticationFailedException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "Attribute");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.webservices.v2_2.Attribute.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "AuthorizationFailedException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.AuthorizationFailedException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "SOAPException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.SOAPException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "ClientException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.ClientException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soap.servlet.moria.feide.no", "UnknownTicketException");
            cachedSerQNames.add(qName);
            cls = no.feide.moria.servlet.soap.UnknownTicketException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call =
                    (org.apache.axis.client.Call) super.service.createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                        java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, sf, df, false);
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public no.feide.moria.webservices.v2_2.Attribute[] getUserAttributes(java.lang.String serviceTicket) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.UnknownTicketException, no.feide.moria.servlet.soap.AuthorizationFailedException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "getUserAttributes"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {serviceTicket});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (no.feide.moria.webservices.v2_2.Attribute[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (no.feide.moria.webservices.v2_2.Attribute[]) org.apache.axis.utils.JavaUtils.convert(_resp, no.feide.moria.webservices.v2_2.Attribute[].class);
            }
        }
    }

    public java.lang.String initiateAuthentication(java.lang.String[] attributes, java.lang.String returnURLPrefix, java.lang.String returnURLPostfix, boolean forceInteractiveAuthentication) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.AuthorizationFailedException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "initiateAuthentication"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {attributes, returnURLPrefix, returnURLPostfix, new java.lang.Boolean(forceInteractiveAuthentication)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
    }

    public no.feide.moria.webservices.v2_2.Attribute[] directNonInteractiveAuthentication(java.lang.String[] attributes, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.AuthenticationFailedException, no.feide.moria.servlet.soap.AuthorizationFailedException, no.feide.moria.servlet.soap.AuthenticationUnavailableException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "directNonInteractiveAuthentication"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {attributes, username, password});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (no.feide.moria.webservices.v2_2.Attribute[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (no.feide.moria.webservices.v2_2.Attribute[]) org.apache.axis.utils.JavaUtils.convert(_resp, no.feide.moria.webservices.v2_2.Attribute[].class);
            }
        }
    }

    public no.feide.moria.webservices.v2_2.Attribute[] proxyAuthentication(java.lang.String[] attributes, java.lang.String proxyTicket) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.UnknownTicketException, no.feide.moria.servlet.soap.AuthorizationFailedException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "proxyAuthentication"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {attributes, proxyTicket});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (no.feide.moria.webservices.v2_2.Attribute[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (no.feide.moria.webservices.v2_2.Attribute[]) org.apache.axis.utils.JavaUtils.convert(_resp, no.feide.moria.webservices.v2_2.Attribute[].class);
            }
        }
    }

    public java.lang.String getProxyTicket(java.lang.String ticketGrantingTicket, java.lang.String proxyServicePrincipal) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.UnknownTicketException, no.feide.moria.servlet.soap.AuthorizationFailedException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "getProxyTicket"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {ticketGrantingTicket, proxyServicePrincipal});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
    }

    public boolean verifyUserExistence(java.lang.String username) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.AuthorizationFailedException, no.feide.moria.servlet.soap.AuthenticationUnavailableException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "verifyUserExistence"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
    }

}
