CREATE TABLE company (
    companyid character varying(255) NOT NULL,
    phonenumber character varying(255) NOT NULL,
    invoicingemailaddress character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    salesemailaddress character varying(255) NOT NULL,
    companyname character varying(255) NOT NULL,
    supportemailaddress character varying(255) NOT NULL,
    companycode character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    deliveryaddress_postaladdressid character varying(255),
    invoicingaddress_postaladdressid character varying(255),
    iban character varying(255) NOT NULL,
    bic character varying(255) NOT NULL,
    host character varying(255),
    termsandconditions character varying(4096)
);
ALTER TABLE public.company OWNER TO hoot;

CREATE TABLE customer (
    customerid character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    phonenumber character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    company boolean NOT NULL,
    emailaddress character varying(255) NOT NULL,
    firstname character varying(255) NOT NULL,
    companyname character varying(255),
    modified timestamp without time zone NOT NULL,
    companycode character varying(255),
    deliveryaddress_postaladdressid character varying(255),
    invoicingaddress_postaladdressid character varying(255),
    owner_companyid character varying(255) NOT NULL
);
ALTER TABLE public.customer OWNER TO hoot;

CREATE TABLE group_ (
    groupid character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    description character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    owner_companyid character varying(255) NOT NULL
);
ALTER TABLE public.group_ OWNER TO hoot;

CREATE TABLE groupmember (
    groupmemberid character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    group_groupid character varying(255),
    user_userid character varying(255)
);

ALTER TABLE public.groupmember OWNER TO hoot;

CREATE TABLE postaladdress (
    postaladdressid character varying(255) NOT NULL,
    addresslinetwo character varying(255),
    addresslinethree character varying(255),
    postalcode character varying(255),
    addresslineone character varying(255),
    city character varying(255),
    country character varying(255)
);
ALTER TABLE public.postaladdress OWNER TO hoot;

CREATE TABLE privilege (
    privilegeid character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    dataid character varying(255),
    key character varying(255) NOT NULL,
    user_userid character varying(255),
    group_groupid character varying(255)
);
ALTER TABLE public.privilege OWNER TO hoot;

CREATE TABLE user_ (
    userid character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    phonenumber character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    emailaddress character varying(255) NOT NULL,
    firstname character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    owner_companyid character varying(255) NOT NULL,
    passwordhash character varying(256) NOT NULL
);
ALTER TABLE public.user_ OWNER TO hoot;

INSERT INTO company VALUES ('3248528E-4D90-41F7-968F-AF255AD16901', '+358 40 1639099', 'invoice@bare.com', '2011-04-22 08:52:13.899', 'sales@bare.com', 'Test Company', 'support@bare.com', '0000000-0', '2011-04-22 08:52:13.899', '4EA7E643-3C80-49B2-8D1C-AAFA7E66A28C', 'CFE997C0-3FAF-4F6C-BBED-DB09689936B6', '-', '-', '*', '-');
INSERT INTO group_ VALUES ('3DE5D850-B015-44C1-904C-86DC2B0276A4', '2012-02-13 21:37:24.804', 'Users', 'user', '2012-02-13 21:37:24.804', '3248528E-4D90-41F7-968F-AF255AD16901');
INSERT INTO group_ VALUES ('1DE5D850-B015-44C1-904C-86DC2B0276A3', '2012-06-25 19:57:00', 'Administrators', 'administrator', '2012-06-25 19:57:00', '3248528E-4D90-41F7-968F-AF255AD16901');
INSERT INTO groupmember VALUES ('50413BBB-DB86-402E-9E98-C7E73F219827', '2013-03-29 19:11:42.986', '1DE5D850-B015-44C1-904C-86DC2B0276A3', 'A591FCB8-772E-4157-B64B-4371A6C7CAE0');
INSERT INTO postaladdress VALUES ('CFE997C0-3FAF-4F6C-BBED-DB09689936B6', '-', '-', '00000', '-', 'Helsinki', 'Finland');
INSERT INTO postaladdress VALUES ('4EA7E643-3C80-49B2-8D1C-AAFA7E66A28C', '-', '-', '00000', '-', 'Helsinki', 'Finland');
INSERT INTO user_ VALUES ('A591FCB8-772E-4157-B64B-4371A6C7CAE0', 'Test', '+123', '2013-03-29 18:21:23.769', 'admin@admin.org', 'User', '2013-03-29 18:21:23.769', '3248528E-4D90-41F7-968F-AF255AD16901', 'c8213c753f70e6ef82a3bbece671c183cc9aa70d944f2d8abbbc50ab7432f2b4');

ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (companyid);

ALTER TABLE ONLY customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (customerid);

ALTER TABLE ONLY group_
    ADD CONSTRAINT group__pkey PRIMARY KEY (groupid);

ALTER TABLE ONLY groupmember
    ADD CONSTRAINT groupmember_pkey PRIMARY KEY (groupmemberid);

ALTER TABLE ONLY postaladdress
    ADD CONSTRAINT postaladdress_pkey PRIMARY KEY (postaladdressid);

ALTER TABLE ONLY privilege
    ADD CONSTRAINT privilege_pkey PRIMARY KEY (privilegeid);

ALTER TABLE ONLY group_
    ADD CONSTRAINT unq_group__0 UNIQUE (owner_companyid, name);

ALTER TABLE ONLY groupmember
    ADD CONSTRAINT unq_groupmember_0 UNIQUE (user_userid, group_groupid);

ALTER TABLE ONLY privilege
    ADD CONSTRAINT unq_privilege_0 UNIQUE (user_userid, group_groupid);

ALTER TABLE ONLY user_
    ADD CONSTRAINT unq_user__0 UNIQUE (owner_companyid, emailaddress);

ALTER TABLE ONLY user_
    ADD CONSTRAINT user__pkey PRIMARY KEY (userid);

ALTER TABLE ONLY company
    ADD CONSTRAINT fk_company_deliveryaddress_postaladdressid FOREIGN KEY (deliveryaddress_postaladdressid) REFERENCES postaladdress(postaladdressid);

ALTER TABLE ONLY company
    ADD CONSTRAINT fk_company_invoicingaddress_postaladdressid FOREIGN KEY (invoicingaddress_postaladdressid) REFERENCES postaladdress(postaladdressid);

ALTER TABLE ONLY customer
    ADD CONSTRAINT fk_customer_billingaddress_postaladdressid FOREIGN KEY (invoicingaddress_postaladdressid) REFERENCES postaladdress(postaladdressid);

ALTER TABLE ONLY customer
    ADD CONSTRAINT fk_customer_deliveryaddress_postaladdressid FOREIGN KEY (deliveryaddress_postaladdressid) REFERENCES postaladdress(postaladdressid);

ALTER TABLE ONLY customer
    ADD CONSTRAINT fk_customer_owner_companyid FOREIGN KEY (owner_companyid) REFERENCES company(companyid);

ALTER TABLE ONLY group_
    ADD CONSTRAINT fk_group__owner_companyid FOREIGN KEY (owner_companyid) REFERENCES company(companyid);

ALTER TABLE ONLY groupmember
    ADD CONSTRAINT fk_groupmember_group_groupid FOREIGN KEY (group_groupid) REFERENCES group_(groupid);

ALTER TABLE ONLY groupmember
    ADD CONSTRAINT fk_groupmember_user_userid FOREIGN KEY (user_userid) REFERENCES user_(userid);

ALTER TABLE ONLY privilege
    ADD CONSTRAINT fk_privilege_group_groupid FOREIGN KEY (group_groupid) REFERENCES group_(groupid);

ALTER TABLE ONLY privilege
    ADD CONSTRAINT fk_privilege_user_userid FOREIGN KEY (user_userid) REFERENCES user_(userid);

ALTER TABLE ONLY user_
    ADD CONSTRAINT fk_user__owner_companyid FOREIGN KEY (owner_companyid) REFERENCES company(companyid);

REVOKE ALL ON TABLE company FROM hoot;
GRANT ALL ON TABLE company TO hoot;
REVOKE ALL ON TABLE customer FROM hoot;
GRANT ALL ON TABLE customer TO hoot;
REVOKE ALL ON TABLE group_ FROM hoot;
GRANT ALL ON TABLE group_ TO hoot;
REVOKE ALL ON TABLE groupmember FROM hoot;
GRANT ALL ON TABLE groupmember TO hoot;
GRANT ALL ON TABLE groupmember TO hoot;
REVOKE ALL ON TABLE postaladdress FROM hoot;
GRANT ALL ON TABLE postaladdress TO hoot;
REVOKE ALL ON TABLE user_ FROM hoot;
GRANT ALL ON TABLE user_ TO hoot;

CREATE TABLE schemaversion
(
  created timestamp without time zone NOT NULL,
  schemaname character varying(255) NOT NULL,
  schemaversion character varying(255) NOT NULL,
  CONSTRAINT schemaversion_pkey PRIMARY KEY (created )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE schemaversion
  OWNER TO hoot;

ALTER TABLE user_ ADD EMAILADDRESSVALIDATED BOOLEAN;
UPDATE user_ SET EMAILADDRESSVALIDATED = true;
ALTER TABLE user_ ALTER COLUMN emailaddressvalidated SET NOT NULL;

CREATE TABLE entry
(
  entryid character varying(255) NOT NULL,
  country character varying(2) NOT NULL,
  created timestamp without time zone NOT NULL,
  key character varying(1024) NOT NULL,
  language character varying(2) NOT NULL,
  modified timestamp without time zone NOT NULL,
  value character varying(1024) NOT NULL,
  owner_companyid character varying(255) NOT NULL,
  basename character varying(1024) NOT NULL,
  path character varying(2048) NOT NULL,
  CONSTRAINT entry_pkey PRIMARY KEY (entryid ),
  CONSTRAINT fk_entry_owner_companyid FOREIGN KEY (owner_companyid)
      REFERENCES company (companyid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entry
  OWNER TO hoot;

ALTER TABLE company ADD COLUMN url character varying(255);
UPDATE COMPANY SET url = 'http://127.0.0.1:8083/groom';

ALTER TABLE entry ADD COLUMN author character varying(1024);

INSERT INTO schemaversion VALUES (NOW(), 'hoot', '0001');

