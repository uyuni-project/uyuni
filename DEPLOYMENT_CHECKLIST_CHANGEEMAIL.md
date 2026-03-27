# Account Email Migration - Deployment & Rollback Checklist

**Project**: Uyuni/Spacewalk
**Component**: Account Email Change (account/ChangeEmail.do → /rhn/account/changeemail)
**Date**: March 26, 2026
**Status**: Ready for Deployment

---

## Pre-Deployment Checklist (Development Team)

### Code Review & Quality
- [ ] Code review completed and approved
- [ ] All comments addressed and resolved
- [ ] No outstanding issues or concerns
- [ ] Code style matches project standards
- [ ] Security review approved
- [ ] Performance impact assessed (minimal)

### Testing
- [ ] Unit tests pass: `mvn test`
- [ ] Integration tests pass on staging
- [ ] Manual QA testing completed
- [ ] Email change functionality verified
- [ ] Admin uid parameter tested
- [ ] Error handling verified
- [ ] CSRF protection confirmed
- [ ] Localization messages display correctly
- [ ] No JavaScript errors in console
- [ ] Works without JavaScript

### Build Verification
- [ ] Maven clean build succeeds: `mvn clean package`
- [ ] No compilation warnings or errors
- [ ] WAR file generated successfully
- [ ] All dependencies resolved
- [ ] Previous build artifacts cleaned

### Documentation Review
- [ ] IMPLEMENTATION_SUMMARY_CHANGEEMAIL.md reviewed
- [ ] MIGRATION_CHANGEEMAIL_COMPLETE.md reviewed
- [ ] QUICKREF_CHANGEEMAIL.md reviewed
- [ ] API documentation updated
- [ ] Deployment procedures documented

---

## Pre-Deployment Checklist (DevOps/Deployment Team)

### Environment Preparation
- [ ] Staging environment verified operational
- [ ] Database backups created
- [ ] Rollback plan documented
- [ ] Monitoring/alerting configured
- [ ] Log rotation configured for new logs
- [ ] Firewall rules verified (no changes needed)

### Dependency Verification
- [ ] Java version compatible (Java 11+)
- [ ] Tomcat version compatible
- [ ] Database accessibility verified
- [ ] External services accessible (if any)
- [ ] Network connectivity verified

### Infrastructure Check
- [ ] Disk space available for WAR file
- [ ] Memory available for application
- [ ] CPU resources adequate
- [ ] No scheduled maintenance during deployment
- [ ] No conflicting deployments planned

---

## Staging Deployment & Verification

### Deployment Execution
- [ ] Backup current WAR file
- [ ] Stop Tomcat/application server
- [ ] Deploy new WAR file to webapps/
- [ ] Start Tomcat/application server
- [ ] Monitor startup logs for errors
- [ ] Wait for application to fully load (2-5 minutes)

### Immediate Verification
- [ ] Application is accessible
- [ ] No errors in application logs
- [ ] No errors in system logs
- [ ] Database connectivity working
- [ ] Web UI is responsive

### Functional Testing (Staging)
- [ ] Can access `/rhn/account/changeemail`
- [ ] Form displays with current email
- [ ] Email input field is HTML5 validated
- [ ] Can change to valid new email
- [ ] Success message displays
- [ ] Can change back to original email
- [ ] Invalid email shows error
- [ ] Same email shows error
- [ ] Admin can access with uid parameter: `?uid=123`
- [ ] CSRF token validation works

### Error Handling Tests (Staging)
- [ ] Test network error (disconnect DB)
- [ ] Test invalid uid (non-existent user)
- [ ] Test empty email
- [ ] Test very long email
- [ ] Test special characters in email
- [ ] Check error messages are appropriate

### Performance Testing (Staging)
- [ ] Response time acceptable (< 100ms)
- [ ] No excessive memory usage
- [ ] No memory leaks after 1 hour
- [ ] Can handle multiple simultaneous requests
- [ ] No database connection exhaustion

### Logs Review (Staging)
- [ ] grep for "ERROR" - should be none
- [ ] grep for "WARN" - should be minimal
- [ ] Check for "AccountEmailController" - should see route initialization
- [ ] No stack traces visible
- [ ] No security warnings

---

## Staging Sign-Off

**Staging Deployment Status**: _____ (PASS / FAIL)

**Issues Found**: (List any issues found)
- 
- 
- 

**Sign-Off**:
- [ ] Development Lead: _________________ Date: _______
- [ ] QA Lead: _________________ Date: _______
- [ ] DevOps Lead: _________________ Date: _______

---

## Production Deployment Plan

### Deployment Window
**Scheduled Date**: ___________
**Scheduled Time**: ___________ to ___________
**Expected Duration**: 10-15 minutes
**Rollback Available**: YES (< 5 minutes)

### Pre-Deployment (30 minutes before)
- [ ] Notify stakeholders of deployment
- [ ] Create pre-deployment database snapshot
- [ ] Gather current application metrics (baseline)
- [ ] Verify no users currently using email change form
- [ ] Notify support team
- [ ] Open monitoring dashboards

### Production Deployment Execution
- [ ] Backup current WAR file (to /var/backups/spacewalk/)
- [ ] Stop Tomcat: `systemctl stop tomcat` (or equivalent)
- [ ] Verify Tomcat stopped (check port 8080 not listening)
- [ ] Deploy new WAR: `cp spacewalk-*.war /opt/tomcat/webapps/`
- [ ] Start Tomcat: `systemctl start tomcat` (or equivalent)
- [ ] Monitor logs in real-time: `tail -f /var/log/uyuni/uyuni.log`
- [ ] Wait for full startup (watch for "AccountEmailController" initialization)
- [ ] Check no errors in first 2 minutes of startup

### Post-Deployment Verification (Immediately After)
- [ ] Access application: https://your-server/rhn/
- [ ] Login works
- [ ] No 500 errors in logs
- [ ] No stack traces visible
- [ ] Web UI responsive
- [ ] Basic navigation works

### Production Functional Testing
- [ ] Navigate to `/rhn/account/changeemail`
- [ ] Form displays with current email
- [ ] Try changing to new valid email
- [ ] Success message displays
- [ ] Database updated (verify in DB)
- [ ] Change back to original email (verify DB again)
- [ ] Test admin access with uid parameter
- [ ] Test error cases

### Performance Monitoring (First Hour)
- [ ] Monitor CPU usage (should remain stable)
- [ ] Monitor memory usage (should remain stable)
- [ ] Monitor response times (should be < 100ms)
- [ ] Monitor error rates (should be 0)
- [ ] Monitor database query times
- [ ] Check no connection pool exhaustion

### Monitoring for 24 Hours
- [ ] Set up alerts for:
  - [ ] HTTP 500 errors
  - [ ] AccountEmailController errors
  - [ ] Database connection pool exhaustion
  - [ ] Memory usage spike
  - [ ] Disk space usage
- [ ] Check logs every few hours
- [ ] Monitor user reports
- [ ] Track application metrics

---

## Post-Deployment Tasks

### Day of Deployment
- [ ] Verify functionality with actual users
- [ ] Update user documentation (if URL referenced)
- [ ] Update internal wiki/docs
- [ ] Update bookmarks/shortcuts
- [ ] Notify staff of URL change (if communicated)
- [ ] Monitor logs for errors

### Week 1 Post-Deployment
- [ ] Review application logs for issues
- [ ] Check performance metrics
- [ ] Verify no user complaints
- [ ] Monitor error rates
- [ ] Update any remaining documentation
- [ ] Plan legacy code removal (Phase 2)

### Ongoing (Monthly)
- [ ] Monitor for deprecated Struts warnings
- [ ] Track any performance degradation
- [ ] Review user feedback
- [ ] Plan legacy code cleanup

---

## Rollback Procedures

### Rollback Decision Criteria
Rollback immediately if ANY of:
- [ ] Application won't start (check logs)
- [ ] Web UI is inaccessible
- [ ] Database connectivity lost
- [ ] Email change functionality broken
- [ ] > 5% of requests return 500 errors
- [ ] > 50% response time increase
- [ ] Critical security issue found
- [ ] Data corruption detected

### Rollback Execution (< 5 minutes)
```bash
# Stop current application
systemctl stop tomcat

# Verify stopped
netstat -tuln | grep 8080  # Should show nothing

# Restore previous WAR
cp /var/backups/spacewalk/spacewalk-old.war /opt/tomcat/webapps/spacewalk.war

# Start application
systemctl start tomcat

# Monitor logs
tail -f /var/log/uyuni/uyuni.log
```

### Rollback Verification
- [ ] Application starts successfully
- [ ] Web UI accessible
- [ ] Email change form accessible at `/account/ChangeEmail.do` (old URL)
- [ ] Functionality works
- [ ] No errors in logs
- [ ] Database restored to pre-deployment state

### Post-Rollback Actions
- [ ] Notify stakeholders of rollback
- [ ] Create incident report
- [ ] Analyze root cause
- [ ] Fix identified issues
- [ ] Plan re-deployment

---

## Emergency Contact

**In case of critical issues during deployment**:

| Role | Name | Phone | Email |
|------|------|-------|-------|
| On-Call DevOps | | | |
| Database Admin | | | |
| Application Owner | | | |
| Security Officer | | | |

---

## Deployment Approval

### Required Sign-Offs

**Project Manager**:
- Name: _________________
- Signature: _________________ Date: _______
- [ ] Approved for production deployment

**Technical Lead**:
- Name: _________________
- Signature: _________________ Date: _______
- [ ] Approved for production deployment

**DevOps Lead**:
- Name: _________________
- Signature: _________________ Date: _______
- [ ] Approved for production deployment

**Security Officer**:
- Name: _________________
- Signature: _________________ Date: _______
- [ ] Approved for production deployment

---

## Deployment Execution Log

**Deployment Date**: _______________
**Deployment Window**: _____________ to _____________
**Deployed By**: _______________

### Timeline

| Time | Action | Status | Notes |
|------|--------|--------|-------|
| 14:00 | Pre-deployment check | | |
| 14:05 | Backup current WAR | | |
| 14:10 | Stop Tomcat | | |
| 14:15 | Deploy new WAR | | |
| 14:20 | Start Tomcat | | |
| 14:25 | Startup verification | | |
| 14:30 | Functional testing | | |
| 14:35 | Performance check | | |
| 14:40 | Stakeholder notification | | |

### Issues Encountered

| Time | Issue | Action Taken | Resolution |
|------|-------|--------------|-----------|
| | | | |
| | | | |

### Final Status

**Deployment Result**: ☐ SUCCESS ☐ PARTIAL ☐ ROLLBACK ☐ FAILED

**Notes**:
_________________________________________________________________
_________________________________________________________________

**Approval Sign-Off**:
- Deployment Manager: _________________ Date: _______

---

## Post-Deployment Verification (24 Hours Later)

- [ ] Application still running
- [ ] No errors in logs
- [ ] Performance metrics normal
- [ ] No user complaints
- [ ] Functionality working correctly
- [ ] Database intact
- [ ] All backups completed
- [ ] No security issues detected

**24-Hour Verification By**: _________________ Date: _______

---

## Legacy Code Removal Schedule (Phase 2)

**Scheduled for**: _________________ (2-4 weeks after successful production deployment)

### Files to Delete
- [ ] `ChangeEmailAction.java`
- [ ] `ChangeEmailSetupAction.java`
- [ ] `ChangeEmailSetupActionTest.java`
- [ ] `yourchangeemail.jsp`
- [ ] `changeemail.jsp` (admin version)

### Struts Config Cleanup
- [ ] Remove deprecated `changeEmailForm` form-bean
- [ ] Remove deprecated action mappings (4 total)
- [ ] Verify no other code references these classes

### Phase 2 Sign-Off
- [ ] Code review approved
- [ ] Final verification passed
- [ ] Deployment completed
- [ ] Legacy code archived (git history preserved)

---

## Document Sign-Off

**Prepared By**: _________________ Date: _______
**Reviewed By**: _________________ Date: _______
**Approved By**: _________________ Date: _______

---

**This checklist should be printed and filed with deployment documentation.**
**Keep this document accessible during deployment for reference.**

Questions? See: IMPLEMENTATION_SUMMARY_CHANGEEMAIL.md

