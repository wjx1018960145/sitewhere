/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.ignite.server;

import java.util.Map;

import com.sitewhere.device.DeviceManagementDecorator;
import com.sitewhere.microservice.ignite.DeviceManagementCacheProviders;
import com.sitewhere.microservice.security.UserContextManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceSpecification;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.device.request.IDeviceSpecificationCreateRequest;
import com.sitewhere.spi.microservice.IMicroservice;
import com.sitewhere.spi.microservice.ignite.IIgniteCacheProvider;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Wraps {@link IDeviceManagement} implementation with Apache Ignite cache
 * support.
 * 
 * @author Derek
 */
public class CacheAwareDeviceManagement extends DeviceManagementDecorator {

    /** Device specification cache */
    private IIgniteCacheProvider<String, IDeviceSpecification> deviceSpecificationCache;

    /** Device cache */
    private IIgniteCacheProvider<String, IDevice> deviceCache;

    /** Device assignment cache */
    private IIgniteCacheProvider<String, IDeviceAssignment> deviceAssignmentCache;

    public CacheAwareDeviceManagement(IDeviceManagement delegate, IMicroservice microservice) {
	super(delegate);
	this.deviceSpecificationCache = new DeviceManagementCacheProviders.DeviceSpecificationCache(microservice, true);
	this.deviceCache = new DeviceManagementCacheProviders.DeviceCache(microservice, true);
	this.deviceAssignmentCache = new DeviceManagementCacheProviders.DeviceAssignmentCache(microservice, true);
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createDevice(com.sitewhere.spi
     * .device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice createDevice(IDeviceCreateRequest device) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.createDevice(device);
	getDeviceCache().setCacheEntry(tenant, result.getHardwareId(), result);
	getLogger().trace("Added created device to cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#getDeviceByHardwareId(java.
     * lang.String)
     */
    @Override
    public IDevice getDeviceByHardwareId(String hardwareId) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.getDeviceByHardwareId(hardwareId);
	if (getDeviceCache().getCacheEntry(tenant, hardwareId) == null) {
	    getDeviceCache().setCacheEntry(tenant, result.getHardwareId(), result);
	    getLogger().trace("Added device to cache.");
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDevice(java.lang.String,
     * com.sitewhere.spi.device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.updateDevice(hardwareId, request);
	getDeviceCache().setCacheEntry(tenant, result.getHardwareId(), result);
	getLogger().trace("Updated device in cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteDevice(java.lang.String,
     * boolean)
     */
    @Override
    public IDevice deleteDevice(String hardwareId, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.deleteDevice(hardwareId, force);
	getDeviceCache().removeCacheEntry(tenant, result.getHardwareId());
	getLogger().trace("Removed device from cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createDeviceAssignment(com.
     * sitewhere.spi.device.request.IDeviceAssignmentCreateRequest)
     */
    @Override
    public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.createDeviceAssignment(request);
	getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	getLogger().trace("Added created assignment to cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#getDeviceAssignmentByToken(
     * java.lang.String)
     */
    @Override
    public IDeviceAssignment getDeviceAssignmentByToken(String token) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.getDeviceAssignmentByToken(token);
	if (getDeviceAssignmentCache().getCacheEntry(tenant, token) == null) {
	    getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	    getLogger().trace("Added assignment to cache.");
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDeviceAssignmentMetadata
     * (java.lang.String, java.util.Map)
     */
    @Override
    public IDeviceAssignment updateDeviceAssignmentMetadata(String token, Map<String, String> metadata)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.updateDeviceAssignmentMetadata(token, metadata);
	getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	getLogger().trace("Updated assignment in cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDeviceAssignmentStatus(
     * java.lang.String, com.sitewhere.spi.device.DeviceAssignmentStatus)
     */
    @Override
    public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.updateDeviceAssignmentStatus(token, status);
	getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	getLogger().trace("Updated assignment in cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteDeviceAssignment(java.
     * lang.String, boolean)
     */
    @Override
    public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.deleteDeviceAssignment(token, force);
	getDeviceCache().removeCacheEntry(tenant, result.getToken());
	getLogger().trace("Removed assignment from cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createDeviceSpecification(com.
     * sitewhere.spi.device.request.IDeviceSpecificationCreateRequest)
     */
    @Override
    public IDeviceSpecification createDeviceSpecification(IDeviceSpecificationCreateRequest request)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.createDeviceSpecification(request);
	getDeviceSpecificationCache().setCacheEntry(tenant, result.getToken(), result);
	getLogger().trace("Added created specification to cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#getDeviceSpecificationByToken(
     * java.lang.String)
     */
    @Override
    public IDeviceSpecification getDeviceSpecificationByToken(String token) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.getDeviceSpecificationByToken(token);
	if (getDeviceAssignmentCache().getCacheEntry(tenant, token) == null) {
	    getDeviceSpecificationCache().setCacheEntry(tenant, result.getToken(), result);
	    getLogger().trace("Added specification to cache.");
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDeviceSpecification(java
     * .lang.String,
     * com.sitewhere.spi.device.request.IDeviceSpecificationCreateRequest)
     */
    @Override
    public IDeviceSpecification updateDeviceSpecification(String token, IDeviceSpecificationCreateRequest request)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.updateDeviceSpecification(token, request);
	getDeviceSpecificationCache().setCacheEntry(tenant, result.getToken(), result);
	getLogger().trace("Added updated specification to cache.");
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteDeviceSpecification(java
     * .lang.String, boolean)
     */
    @Override
    public IDeviceSpecification deleteDeviceSpecification(String token, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.deleteDeviceSpecification(token, force);
	getDeviceSpecificationCache().removeCacheEntry(tenant, result.getToken());
	getLogger().trace("Removed specification from cache.");
	return result;
    }

    protected IIgniteCacheProvider<String, IDeviceSpecification> getDeviceSpecificationCache() {
	return deviceSpecificationCache;
    }

    protected void setDeviceSpecificationCache(
	    IIgniteCacheProvider<String, IDeviceSpecification> deviceSpecificationCache) {
	this.deviceSpecificationCache = deviceSpecificationCache;
    }

    protected IIgniteCacheProvider<String, IDevice> getDeviceCache() {
	return deviceCache;
    }

    protected void setDeviceCache(IIgniteCacheProvider<String, IDevice> deviceCache) {
	this.deviceCache = deviceCache;
    }

    protected IIgniteCacheProvider<String, IDeviceAssignment> getDeviceAssignmentCache() {
	return deviceAssignmentCache;
    }

    protected void setDeviceAssignmentCache(IIgniteCacheProvider<String, IDeviceAssignment> deviceAssignmentCache) {
	this.deviceAssignmentCache = deviceAssignmentCache;
    }
}