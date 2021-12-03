create table email_domain
(
    email_domain_id UNIQUEIDENTIFIER NOT NULL
        CONSTRAINT email_domain_pk PRIMARY KEY,
    name            VARCHAR(100)     NOT NULL,
    description     VARCHAR(200)
);

INSERT INTO email_domain (email_domain_id, name, description)
VALUES (newid(), '%advancecharity.org.uk', 'ADVANCE'),
       (newid(), '%bidvestnoonan.com', 'BIDVESTNOONA'),
       (newid(), '%bsigroup.com', 'BSIGROUP'),
       (newid(), '%careuk.com', 'CAREUK'),
       (newid(), '%catch-22.org.uk', 'CATCH22'),
       (newid(), '%catch22uk.onmicrosoft.com', 'CATCH22ONMIC'),
       (newid(), '%changinglives.net', 'CHANGINGCJSM'),
       (newid(), '%changing-lives.org.uk', 'CHANGINGLIVE'),
       (newid(), '%.cjsm.net', 'CJSM'),
       (newid(), '%combined.nhs.uk', 'COMBINEDNHS'),
       (newid(), '%dtvcrcsecure.co.uk', 'CRCSEC'),
       (newid(), '%crgmedical.uk.com', 'CRGMEDICALUK'),
       (newid(), '%cumbriagateway.co.uk', 'CUMBRIAGATEW'),
       (newid(), '%durham.gov.uk', 'DURHAM'),
       (newid(), '%dxc.com', 'DXC'),
       (newid(), '%cgi.com', 'DXCBRIDGEND'),
       (newid(), '%forwardtrust.org.uk', 'FORWARDTRUST'),
       (newid(), '%foundationuk.org', 'FOUNDATIONUK'),
       (newid(), 'uk.g4s.com', 'G4S'),
       (newid(), '%gcemployment.uk', 'GCEMPLOYMENT'),
       (newid(), '%geoamey.co.uk', 'GEO'),
       (newid(), '%gmmh.nhs.uk', 'GMMHNHS'),
       (newid(), '%.gse.gov.uk', 'GSEGOVUK'),
       (newid(), '%.gsi.gov.uk', 'GSI'),
       (newid(), '%hibiscus.org.uk', 'HIBISCUS'),
       (newid(), '%hmiprisons.gov.uk', 'HMIPRISONS'),
       (newid(), '%homeoffice.gov.uk', 'HOMOFF'),
       (newid(), '%ingeus.co.uk', 'INGEUS'),
       (newid(), '%inspira.org.uk', 'INSPIRA'),
       (newid(), '%inspirenorth.co.uk', 'INSPIRENORTH'),
       (newid(), 'interservejustice.org', 'INTERSERVE'),
       (newid(), '%interventionsalliance.co.uk', 'INTERVENTIO2'),
       (newid(), '%interventionsalliance.com', 'INTERVENTION'),
       (newid(), '%justice.gov.uk', 'JUSTICE'),
       (newid(), '%kaleidoscopeproject.org.uk', 'KALEIDOSCOPE'),
       (newid(), '%ksscrc.co.uk', 'KSSCRC'),
       (newid(), '%lancashirewomen.org', 'LANCSWOMEN'),
       (newid(), '%latcharity.org.uk', 'LATCHARITY'),
       (newid(), '%layobservers.co.uk', 'LAYOBSERVERS'),
       (newid(), '%leeds.gov.uk', 'LEEDS'),
       (newid(), 'lincolnshire.gov.uk', 'LINCOLNSHIRE'),
       (newid(), 'londoncrc.org.uk', 'LONDONCRC'),
       (newid(), '%maximusuk.co.uk', 'MAXIMUSUK'),
       (newid(), 'mtcnovo.co.uk', 'MCTNOVO'),
       (newid(), '%mtcgroup.org.uk', 'MCTNOVO2'),
       (newid(), '%medicspro.uk', 'MEDICSPRO'),
       (newid(), 'merseycare.nhs.uk', 'MERSEYCARE'),
       (newid(), '%digital.justice.gov.uk', 'MOJDIGITAL'),
       (newid(), '%mpft.nhs.uk', 'MPFTNHS'),
       (newid(), '%mungos.org', 'MUNGOS'),
       (newid(), '%nacro.org.uk', 'NACRO'),
       (newid(), '%ncic.nhs.uk', 'NCICNHS'),
       (newid(), '%nelsontrust.com', 'NELSONTRUST'),
       (newid(), '%newleafproject.org.uk', 'NEWLEAFPROJE'),
       (newid(), '%nhft.nhs.uk', 'NHFTNHSUK'),
       (newid(), '%nhs.net', 'NHS'),
       (newid(), '%nottscc.gov.uk', 'NOTTSCC'),
       (newid(), '%nottshc.nhs.uk', 'NOTTSHCNHS'),
       (newid(), '%notts.nhs.uk', 'NOTTSNHS'),
       (newid(), '%nottinghamwomenscentre.com', 'NOTTSWC'),
       (newid(), '%npt.gov.uk', 'NPT'),
       (newid(), '%oxfordhealth.nhs.uk', 'OXFORDHEALTH'),
       (newid(), '%pecan.org.uk', 'PECAN'),
       (newid(), '%pnn.police.uk', 'POLICE'),
       (newid(), '%police.uk', 'POLICE1'),
       (newid(), '%ppo.gov.uk', 'PPO'),
       (newid(), '%practiceplusgroup.com', 'PRACTICEPLUS'),
       (newid(), '%prisonadvice.org.uk', 'PRISONADVICE'),
       (newid(), 'hmiprobation.gov.uk', 'PROBATION'),
       (newid(), '%pss.org.uk', 'PSS'),
       (newid(), '%remploy.co.uk', 'REMPLOY'),
       (newid(), '%restorativewales.org.uk', 'RESTORAWALES'),
       (newid(), 'rrp.gse.gov.uk', 'RRPGSE'),
       (newid(), '%salford.gov.uk', 'SALFORD'),
       (newid(), '%sanctuary.uk', 'SANCTUARYUK'),
       (newid(), '%seetec.co.uk', 'SEETEC'),
       (newid(), '%seetec.com', 'SEETECCOM'),
       (newid(), '%serco.com', 'SERCO'),
       (newid(), '%sheffield.gov.uk', 'SHEFFIELD'),
       (newid(), '%shelter.org.uk', 'SHELTER'),
       (newid(), '%shp.org.uk', 'SHPORGUK'),
       (newid(), '%shproject.net', 'SHPROJECT'),
       (newid(), '%slam.nhs.uk', 'SLAMNHS'),
       (newid(), '%sodexojusticeservices.com', 'SODEXO'),
       (newid(), '%sodexojusticeservices.com.cjsm.net', 'SODEXO1'),
       (newid(), '%probation.sodexogov.co.uk', 'SODEXO2'),
       (newid(), '%sodexo.gov.uk', 'SODEXOGOV'),
       (newid(), '%sodexogov.co.uk', 'SODEXOGOVCOU'),
       (newid(), 'southglos.gov.uk', 'SOUTHGLOS'),
       (newid(), '%spectrum-cic.nhs.uk', 'SPECTRUMCIC'),
       (newid(), '%gov.sscl.com', 'SSCL'),
       (newid(), '%stgilestrust.org.uk', 'STGILESTRUST'),
       (newid(), '%surrey.pnn.police.uk', 'SURPOL'),
       (newid(), '%sussexpathways.org.uk', 'SUSSEXPATHWA'),
       (newid(), 'thamesvalleycrc.org.uk', 'THAMESVALLEY'),
       (newid(), '%thewisegroup.co.uk', 'THEWISEGROUP'),
       (newid(), '%thirteengroup.co.uk', 'THIRTEENGROU'),
       (newid(), '%togetherwomen.org', 'TOGETHERWOME'),
       (newid(), '%tomorrowswomen.org.uk', 'TOMORROWSWOM'),
       (newid(), '%thamesvalleypartnership.org.uk', 'TVPARTNERSHI'),
       (newid(), '%virgincare.co.uk', 'VIRGINCARE'),
       (newid(), '%wales.nhs.uk', 'WALESNHS'),
       (newid(), '%womenscentrecornwall.org.uk', 'WCCORNWALL'),
       (newid(), '%womenscommunitymatters.org', 'WCMATTERS'),
       (newid(), '%willowdenefarm.org.uk', 'WILLOWDENEFA'),
       (newid(), '%wipuk.org', 'WIPUK'),
       (newid(), 'workinglinkssecure.co.uk', 'WLSECURE'),
       (newid(), '%woking.gov.uk', 'WOKING'),
       (newid(), '%womenoutwest.co.uk', 'WOMENOUTWEST'),
       (newid(), '%womenscentre.org.uk', 'WOMENSCENTRE'),
       (newid(), '%womens-work.org.uk', 'WOMENSWORK');