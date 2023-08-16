import {useContext, useState, useEffect, useRef } from "react";
import { 
    Checkbox, Heading, TextInput, Select, SelectItem, Button, Grid, Column,Tile,
    DataTable, TableContainer, Table, TableHead, TableRow, TableHeader, TableBody, TableCell, Section
    } from '@carbon/react';
    import { Search} from '@carbon/react';
    import { getFromOpenElisServer, postToOpenElisServerFullResponse, hasRole } from "../utils/Utils";
import { NotificationContext } from "../layout/Layout";
import {AlertDialog} from "../common/CustomNotification";
import { FormattedMessage, injectIntl } from 'react-intl'
import "./../pathology/PathologyDashboard.css"

function ImmunohistochemistryDashboard() {

  const componentMounted = useRef(false);

  const { notificationVisible ,setNotificationVisible,setNotificationBody} = useContext(NotificationContext);
  const [counts ,setCounts] = useState({ inProgress: 0, awaitingReview: 0, additionalRequests: 0, complete: 0});
  const [statuses, setStatuses] = useState([]);
  const [immunohistochemistryEntries, setImmunohistochemistryEntries] = useState([])
  const [filters, setFilters] = useState({searchTerm: "", myCases: false, statuses: []});

  function formatDateToDDMMYYYY(date) {
    var day = date.getDate();
    var month = date.getMonth() + 1; // Month is zero-based
    var year = date.getFullYear();

    // Ensure leading zeros for single-digit day and month
    var formattedDay = (day < 10 ? '0' : '') + day;
    var formattedMonth = (month < 10 ? '0' : '') + month;

    // Construct the formatted string
    var formattedDate = formattedDay + '/' + formattedMonth + '/' + year;
    return formattedDate;
  }

  const getPastWeek = () => {
    // Get the current date
    var currentDate = new Date();

    // Calculate the date of the past week
    var pastWeekDate = new Date(currentDate);
    pastWeekDate.setDate(currentDate.getDate() - 7);

    return formatDateToDDMMYYYY(currentDate) + " - " + formatDateToDDMMYYYY(pastWeekDate);
  }

  const tileList = [
    {"title" : "Cases in Progress"  , "count" : counts.inProgress} ,
    {"title" : "Awaiting Immunohistochemistry Review"  , "count" : counts.awaitingReview},
    {"title" : "Additional Immunohistochemistry Requests"  , "count" : counts.additionalRequests},
    {"title" : "Complete (Week " + getPastWeek() + " )"   , "count" : counts.complete}
  ]

  const setStatusList = (statusList) => {
    if (componentMounted.current) {
        setStatuses(statusList);
    }
  }



  const assignCurrentUserAsTechnician = (event, immunohistochemistrySampleId) => {
    postToOpenElisServerFullResponse("/rest/immunohistochemistry/assignTechnician?immunohistochemistrySampleId=" + immunohistochemistrySampleId, {}, refreshItems)
  }

  const assignCurrentUserAsPathologist = (event, immunohistochemistrySampleId) => {
    postToOpenElisServerFullResponse("/rest/immunohistochemistry/assignPathologist?immunohistochemistrySampleId=" + immunohistochemistrySampleId, {}, refreshItems)
  }

  const renderCell = (cell, row) => {
    var status = row.cells.find(
      (e) => e.info.header === 'status'
    ).info.header.status;
    var immunohistochemistrySampleId = row.id;
    
    if (cell.info.header === 'assignedTechnician' && !cell.value ) {
      return <TableCell key={cell.id}>
        <Button type="button" onClick={(e) => {assignCurrentUserAsTechnician(e, immunohistochemistrySampleId)}}>Start</Button>
      </TableCell>
    }
    if (cell.info.header === 'assignedPathologist' && !cell.value && status === 'READY_PATHOLOGIST' && hasRole("Pathologist")) {
      return <TableCell key={cell.id}>
        <Button type="button" onClick={(e) => {assignCurrentUserAsPathologist(e, immunohistochemistrySampleId)}}>Start</Button>
      </TableCell>
    } else {
      return <TableCell key={cell.id}>{cell.value}</TableCell>
    }
  }

  const setImmunohistochemistryEntriesWithIds = (entries) => {
    if (componentMounted.current && entries && entries.length > 0) {
      var i = 0;
      setImmunohistochemistryEntries(entries.map((entry) => {
        return {...entry, id: '' + entry.immunohistochemistrySampleId};
      }));
    }
    
  }

  const setStatusFilter = (event) => {
    if (event.target.value === 'All') {
      setFilters({...filters, statuses: statuses});
    } else {
      setFilters({...filters, statuses: [{"id": event.target.value}]});
    }
  }
  const loadCounts = (data) => {
    setCounts(data);
  }

  const filtersToParameters = () => {
    return "statuses=" + filters.statuses.map((entry) => {
      return entry.id;
    }).join(",")+ "&searchTerm="+filters.searchTerm;
  }

  const refreshItems = () => {
    getFromOpenElisServer("/rest/immunohistochemistry/dashboard?" + filtersToParameters(), setImmunohistochemistryEntriesWithIds);
  }

  const openCaseView = (id) => {
    window.location.href = "/ImmunohistochemistryCaseView/" + id;
  }

  useEffect(() => {
    componentMounted.current = true;
    getFromOpenElisServer("/rest/displayList/IMMUNOHISTOCHEMISTRY_STATUS", setStatusList);
    getFromOpenElisServer("/rest/immunohistochemistry/dashboard/count",  loadCounts);

    return () => {
      componentMounted.current = false;
    }
  }, []);

  useEffect(() => {
    componentMounted.current = true;
    setFilters({...filters, statuses: statuses});

    return () => {
      componentMounted.current = false
    }
  }, [statuses]);

  

  useEffect(() => {
    componentMounted.current = true;
    refreshItems();
    return () => {
      componentMounted.current = false
    }
  }, [filters]);

  return (
    <>
        {notificationVisible === true ? <AlertDialog/> : ""}
        <Grid fullWidth={true}>
        <Column lg={16}>
          <Section>
            <Section >
              <Heading >
                <FormattedMessage id="immunohistochemistry.label.title" />
              </Heading>
            </Section>
          </Section>
        </Column>
      </Grid>
      <div className="dashboard-container">
      {tileList.map((tile, index) => (
          <Tile  key={index} className="dashboard-tile">
            <h3 className="tile-title">{tile.title}</h3>
            <p className="tile-value">{tile.count}</p>
          </Tile>
          ))}
      </div>
        <Grid fullWidth={true} className="gridBoundary">
        <Column lg={8} md={4} sm={2}>
        <Search
              size="sm"
              value={filters.searchTerm}
              onChange={(e) => setFilters({...filters, searchTerm: e.target.value})}
              placeholder='Search by LabNo or Family Name'
              labelText="Search by LabNo or Family Name"
            />
        </Column>
        <Column lg={8} md={4} sm={2}>
            <div className="inlineDivBlock">
            <div >Filters:</div>
            <Checkbox labelText="My cases" 
              id="filterMyCases"
              value={filters.myCases}
              onChange={(e) => setFilters({...filters, myCases: e.target.checked})}/>
            <Select id="statusFilter"
                    name="statusFilter"
                    labelText="Status"
                    defaultValue="placeholder"
                    onChange={setStatusFilter}
                    noLabel
                    >
                        <SelectItem disabled hidden value="placeholder" text="Status"/>
                        <SelectItem text="All" value="All"
                        />
                    {statuses.map((status, index) => {
                        return (<SelectItem key={index}
                                            text={status.value}
                                            value={status.id}
                        />);
                    })}</Select></div>
        </Column>
        
        <Column lg={16} md={8} sm={4}>
        <DataTable rows={immunohistochemistryEntries} headers={[
    {
      key: 'requestDate',
      header: 'Request Date',
    },
    {
      key: 'status',
      header: 'Stage',
    },
    {
      key: 'lastName',
      header: 'Last Name',
    },
    {
      key: 'firstName',
      header: 'First Name',
    },
    {
      key: 'assignedTechnician',
      header: 'Assigned Technician',
    },
    {
      key: 'assignedPathologist',
      header: 'Assigned Pathologist',
    },
    {
      key: 'labNumber',
      header: 'Lab Number',
    },
  ]} isSortable >
                    {({ rows, headers, getHeaderProps, getTableProps }) => (
                        <TableContainer title="" description="">
                            <Table {...getTableProps()}>
                                <TableHead>
                                    <TableRow>
                                        {headers.map((header) => (
                                            <TableHeader {...getHeaderProps({ header })}>
                                                {header.header}
                                            </TableHeader>
                                        ))}
                                    </TableRow>

                                </TableHead>
                                <TableBody>
                                <>
                                  {rows.map((row) => (
                                    <TableRow key={row.id} onClick={() => {openCaseView(row.id)}}>
                                      {row.cells.map((cell) => (
                                        renderCell(cell, row)
                                      ))}
                                    </TableRow>
                                  ))}
                                </>
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </DataTable>
                </Column>
                </Grid>
    </>
)
}

export default ImmunohistochemistryDashboard;